import interfaces.impl.LazyRefillCountRateLimiter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class LazyRefillCountRateLimiterTest {

    @Nested
    @DisplayName("when request limit is 10 in 10 seconds")
    class WhenLimitIs10 {
        private var rateLimiter: LazyRefillCountRateLimiter? = null
        private var username: String? = null
        private var numberOfRequests: Int = 10
        private var tenSecondsInMillis: Long = 10 * 1000L

        @BeforeEach
        fun beforeEachTest() {
            rateLimiter = LazyRefillCountRateLimiter(numberOfRequests, tenSecondsInMillis)
            username = "andre"
        }

        @Test
        @DisplayName("user gets limited after performing available requests")
        fun userGetsLimited() {
            val userRequests = numberOfRequests + 1

            val responses = IntArray(userRequests).map {
                rateLimiter!!.enter(username!!)
            }
            assertEquals(1, responses.count { !it.allowed })
        }

        @Test
        @DisplayName("user gets access after waiting the expected time")
        fun userGetsHasAccessToDataAfterWaiting() {
            val userRequests = numberOfRequests + 1
            val waiter = CountDownLatch(1)
            val waitingTime = IntArray(userRequests).map {
                rateLimiter!!.enter(username!!)
            }.last().allowedAfter


            waiter.await(waitingTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            val response = rateLimiter!!.enter(username!!)

            assertEquals(true, response.allowed)
        }
    }
}