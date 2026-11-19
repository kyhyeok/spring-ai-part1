package com.example.rag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Section 6. RAG 개념과 Vector Store 기초 — 개념 확인용 CLI 데모
 *
 * 실행:
 *   export OPENAI_API_KEY=sk-...
 *   ./gradlew bootRun --args='--demo=similarity'
 *   ./gradlew bootRun --args='--demo=chunking'
 *   ./gradlew bootRun --args='--demo=minirag'
 */
@SpringBootApplication
class Section06Application

fun main(args: Array<String>) {
    runApplication<Section06Application>(*args)
}
