package org.postpi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PostPiApplication

fun main(args: Array<String>) {
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))
    runApplication<PostPiApplication>(*args)
}
