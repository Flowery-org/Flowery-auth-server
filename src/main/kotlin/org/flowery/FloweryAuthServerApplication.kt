package org.flowery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FloweryAuthServerApplication

fun main(args: Array<String>) {
    runApplication<FloweryAuthServerApplication>(*args)
}
