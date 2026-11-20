package com.example.rag.demo

import org.springframework.ai.document.Document
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/** --demo 인자에 따라 해당 데모를 실행한다. */
@Component
class DemoRunner(
    private val similarityDemo: SimilarityDemo,
    private val chunkingDemo: ChunkingDemo,
    private val miniRagDemo: MiniRagDemo,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        // getOptionValues는 옵션이 없으면 null, 값 없이 --demo만 주면 빈 리스트 → 둘 다 기본값
        val demo = args.getOptionValues("demo")?.firstOrNull() ?: "similarity"

        println()
        when (demo) {
            "similarity" -> similarityDemo.run()
            "chunking" -> chunkingDemo.run()
            "minirag" -> miniRagDemo.run()
            else -> {
                println("알 수 없는 데모: $demo")
                println("사용 가능: similarity | chunking | minirag")
            }
        }
        println()
    }
}

/**
 * Java 텍스트 블록과 같은 문자열을 만든다.
 * 공통 들여쓰기·줄 끝 공백을 지우고, 닫는 """가 따로 한 줄일 때처럼 끝에 줄바꿈을 붙인다.
 *
 * Java와 다른 점 (텍스트 블록을 옮길 때 확인):
 * - 닫는 """의 위치는 들여쓰기 계산에 들어가지 않는다 (Java는 들어간다)
 * - `\n`·`\t`·`\s`·줄 끝 `\` 같은 이스케이프는 처리하지 않고, `$`는 문자열 템플릿으로 해석된다
 */
internal fun String.textBlock(): String =
    // 줄 끝 공백 판정은 Java와 같은 Character.isWhitespace (Kotlin isWhitespace는 NBSP까지 지운다)
    trimIndent().lines().joinToString("\n") { line -> line.trimEnd { Character.isWhitespace(it) } } + "\n"

/**
 * Spring AI 2.x `Document.getText()`는 `@Nullable`(미디어 문서는 텍스트가 없음)이다.
 * TextReader·TokenTextSplitter·SimpleVectorStore가 돌려준 문서는 항상 텍스트가 있으므로,
 * 없으면 원인 메시지와 함께 실패한다 (Java는 텍스트를 처음 쓰는 자리에서 NPE).
 */
internal val Document.requiredText: String
    get() = checkNotNull(text) { "텍스트가 없는 Document: $id" }
