import interfaces.RateLimiter
import interfaces.impl.LazyRefillCountRateLimiter
import models.Response
import java.sql.Time
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    printMessage("NOW ${Time.from(Instant.now())}")

    val period = 60 * 60 * 1 * 1000L  // one 1h has 60m, 1m has 60s, one second has 1000ms
    val limit = 100
    val rt: RateLimiter = LazyRefillCountRateLimiter(limit, period)

//    runWithThreadPool(rt, limit, period)
    runLinear(rt, limit)
}

private fun buildMessage(res: Response): String {
    val duration = Duration.between(Instant.now(), Instant.ofEpochMilli(res.allowedAfter))

    return if (!res.allowed) {
        var secondsMessage = ""
        secondsMessage = if (duration.toSeconds() == 0L) {
            "Less than 1 second until next available interval"
        } else {
            "Try again in ${duration.toSeconds()} seconds"
        }
        "429: Rate limit exceeded. $secondsMessage"
    } else {
        "200: Processed successfully ${Time.from(Instant.now())}"
    }
}

private fun printMessage(message: String) {
    println(message)
    println("--------------------------------------------------------------")
}

private fun RateLimiter.executeCommand(key: String) = this.enter(key).let(::buildMessage).let(::printMessage)

private fun runLinear(rt: RateLimiter, limit: Int, mismatchInMillis: Long = 333L) {
    val requests = limit * 10

    for (i in 0 until requests) {
        rt.executeCommand("andre")
        Thread.sleep(mismatchInMillis)
    }
}

private fun runWithThreadPool(
    rt: RateLimiter,
    limit: Int,
    period: Long,
    mismatchInMillis: Long = 333L
) {
    val requests = limit * 10
    val pool = Executors.newScheduledThreadPool(5);

    for (i in 0 until requests) {
        pool.scheduleAtFixedRate({ rt.executeCommand("andre") }, 0, period, TimeUnit.MILLISECONDS)
        Thread.sleep(mismatchInMillis) // blocking main thread to create some mismatch on the cycles
    }
}
