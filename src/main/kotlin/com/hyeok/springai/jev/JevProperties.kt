package com.hyeok.springai.jev

import org.springframework.boot.context.properties.ConfigurationProperties

// 설정에 값이 없을 때 Java record와 같게 바인딩되도록 한다.
// - enabled: Java의 boolean처럼 false
// - 문자열: Java처럼 null
@ConfigurationProperties(prefix = "jev")
data class JevProperties(
    val enabled: Boolean = false,
    val apiKey: String?,
    val baseUrl: String?,
    val model: String?,
)
