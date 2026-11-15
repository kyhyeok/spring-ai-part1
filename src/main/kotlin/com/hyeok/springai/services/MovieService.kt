package com.hyeok.springai.services

import com.hyeok.springai.model.Movie
import com.hyeok.springai.model.ReviewAnalysis

interface MovieService {

    fun findMovie(title: String): Movie?

    fun findMoviesByDirector(director: String, count: Int): List<Movie>?

    fun askRawJsonTrap(title: String): String?

    fun askRawJsonFixed(title: String): String?

    fun describeFormat(): String

    // LLM 버전은 entity()가 @Nullable이라 nullable, Jev 버전은 항상 결과를 만들어 non-null
    fun analyzeReviewWithLlm(review: String): ReviewAnalysis?

    fun analyzeReviewWithJev(review: String): ReviewAnalysis
}
