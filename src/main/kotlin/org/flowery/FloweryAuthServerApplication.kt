package org.flowery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
class FloweryAuthServerApplication

fun main(args: Array<String>) {
    runApplication<FloweryAuthServerApplication>(*args)
}
