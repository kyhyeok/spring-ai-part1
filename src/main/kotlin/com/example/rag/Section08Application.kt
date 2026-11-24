package com.example.rag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

/** Section 8. RAG 품질 개선 — 튜닝과 평가 */
@SpringBootApplication
class Section08Application {

    // Java의 new ObjectMapper()에 Kotlin 모듈을 더한 것 — data class(EvaluationSet)를 생성자로 역직렬화
    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
}

fun main(args: Array<String>) {
    runApplication<Section08Application>(*args)
}
