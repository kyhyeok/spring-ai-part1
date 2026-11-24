package com.example.rag.eval

import com.example.rag.tuning.TuningService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Section 8 실습 API
 *
 *  POST /api/tuning/ingest-all   청크 크기별(200/500/1000) 적재
 *  GET  /api/tuning/compare      청크 크기별 검색 결과 비교
 *  GET  /api/tuning/search       검색 파라미터 실험 (topK / threshold / filter)
 *  GET  /api/rag/ask             현재 설정으로 질의응답 (근거 포함)
 *  POST /api/eval/run            평가 세트 전체 실행 -> 리포트
 */
@RestController
class EvalController(
    private val tuningService: TuningService,
    private val ragService: RagService,
    private val evaluationService: EvaluationService,
) {

    // S6
    @PostMapping("/api/tuning/ingest-all")
    fun ingestAll(): Map<String, Any>? = null

    // S6
    @GetMapping("/api/tuning/compare")
    fun compare(
        @RequestParam q: String,
        @RequestParam(defaultValue = "2") topK: Int,
    ): Map<String, Any>? = null

    // S9
    @GetMapping("/api/tuning/search")
    fun search(
        @RequestParam q: String,
        @RequestParam(defaultValue = "4") topK: Int,
        @RequestParam(defaultValue = "0.0") threshold: Double,
        @RequestParam(required = false) chunkSize: Int?,
        @RequestParam(required = false) category: String?,
    ): Map<String, Any>? = null

    // S9
    @GetMapping("/api/rag/ask")
    fun ask(@RequestParam q: String): Map<String, Any> {

        // 값이 아직 비어 있다(null). Java Map.of는 null 값을 받지 않아 NPE(500)가 나므로,
        // 같은 동작을 위해 이 빈자리에서는 mapOf 대신 java.util.Map.of를 쓴다
        return java.util.Map.of<String, Any>(
            "question", null,
            "answer", null,
            "evidenceCount", null,
            "contextTokensApprox", null,
        )
    }

    // S10
    @PostMapping("/api/eval/run")
    fun runEvaluation(): EvaluationService.Report? = null
}
