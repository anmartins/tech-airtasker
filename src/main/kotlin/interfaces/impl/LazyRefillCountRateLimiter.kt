package interfaces.impl

import interfaces.RateLimiter
import models.Response
import java.sql.Time
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class LazyRefillCountRateLimiter(
    private val limitNumber: Int,
    private val intervalInMillis: Long,
) : RateLimiter {

    private val requests = ConcurrentHashMap<String, Node>()

    data class Node(
        var lastRefillTime: Long,
        var availableRequests: AtomicInteger,
        var validUntil: Long,
    )

    override fun enter(id: String): Response {
        val now = Time.from(Instant.now()).toInstant().toEpochMilli()

        requests.computeIfAbsent(id) {
            Node(
                lastRefillTime = now,
                availableRequests = AtomicInteger(limitNumber),
                validUntil = now + intervalInMillis
            )

        }.let { node ->
            refill(node)
            val availableRequests = node.availableRequests.getAndUpdate {
                if (it > 0) it - 1 else it
            }

            return Response(allowed = availableRequests > 0, allowedAfter = node.validUntil )
        }
    }

    private fun refill(node: Node): Node {
        val now = Time.from(Instant.now()).toInstant().toEpochMilli()

        val timeBetweenRefills = (now - node.lastRefillTime)
        val interval = timeBetweenRefills / intervalInMillis
        val requestsToAdd = interval * limitNumber

        if (requestsToAdd > 0) {
            val newAvailableRequests = min(node.availableRequests.getAcquire() + requestsToAdd.toInt(), limitNumber)
            node.lastRefillTime = now
            node.validUntil = now + intervalInMillis
            node.availableRequests.setRelease(newAvailableRequests)
        }
        return node
    }
}

