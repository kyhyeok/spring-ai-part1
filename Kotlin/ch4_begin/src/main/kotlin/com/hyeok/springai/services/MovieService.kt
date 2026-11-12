package com.hyeok.springai.services

import com.hyeok.springai.model.Movie

interface MovieService {

    fun findMovie(title: String): Movie?

    fun findMoviesByDirector(director: String, count: Int): List<Movie>?

    fun askRawJsonTrap(title: String): String?

    fun askRawJsonFixed(title: String): String?

    fun describeFormat(): String

//    fun analyzeReviewWithLlm(review: String): ReviewAnalysis?
//
//    fun analyzeReviewWithJev(review: String): ReviewAnalysis?
}
