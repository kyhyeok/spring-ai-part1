package com.hyeok.springai.controllers

import com.hyeok.springai.model.Movie
import com.hyeok.springai.services.MovieService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/movies")
class MovieController(
    private val movieService: MovieService,
) {

    // entity()·content()가 @Nullable이라 반환형도 nullable. null이면 Java와 같이 빈 본문 200
    @GetMapping("/one")
    fun one(@RequestParam(defaultValue = "기생충") title: String): Movie? =
        movieService.findMovie(title)

    @GetMapping("/by-director")
    fun byDirector(
        @RequestParam(defaultValue = "봉준호") director: String,
        @RequestParam(defaultValue = "3") count: Int,
    ): List<Movie>? = movieService.findMoviesByDirector(director, count)

    @GetMapping("/raw-json-trap")
    fun rawJsonTrap(@RequestParam(defaultValue = "기생충") title: String): String? =
        movieService.askRawJsonTrap(title)

    @GetMapping("/raw-json-fixed")
    fun rawJsonFixed(@RequestParam(defaultValue = "기생충") title: String): String? =
        movieService.askRawJsonFixed(title)

    /** [S8~S10 노트] entity()가 자동 주입하는 포맷 지시문(JSON 스키마) 확인 */
    @GetMapping("/format")
    fun format(): String = movieService.describeFormat()
}
