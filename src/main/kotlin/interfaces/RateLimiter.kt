package interfaces

import models.Response

interface RateLimiter {
    fun enter(id: String) : Response
}

