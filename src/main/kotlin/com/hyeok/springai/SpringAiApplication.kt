package com.hyeok.springai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringAiApplication

fun main(args: Array<String>) {
    runApplication<SpringAiApplication>(*args)
}
