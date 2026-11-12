package com.hyeok.springai.jev

//@ConfigurationProperties(prefix = "jev")
// 설정에 값이 없을 때 Java record와 같게 바인딩되도록 한다.
// - enabled: Java의 boolean처럼 false (기본값이 없으면 바인딩 단계에서 기동이 실패한다)
// - 문자열: Java처럼 null
data class JevProperties(
    val enabled: Boolean = false,
    val apiKey: String?,
    val baseUrl: String?,
    val model: String?,
)
