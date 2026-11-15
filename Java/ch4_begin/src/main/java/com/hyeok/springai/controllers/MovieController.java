package com.hyeok.springai.controllers;

import com.hyeok.springai.jev.JevClient;
import com.hyeok.springai.model.Movie;
import com.hyeok.springai.model.ReviewAnalysis;
import com.hyeok.springai.services.MovieService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;
	private final JevClient jevClient;

	public MovieController(MovieService movieService, JevClient jevClient) {
        this.movieService = movieService;
		this.jevClient = jevClient;
	}

    @GetMapping("/one")
    public Movie one(@RequestParam(defaultValue = "기생충") String title) {
        return movieService.findMovie(title);
    }

    @GetMapping("/by-director")
    public List<Movie> byDirector(@RequestParam(defaultValue = "봉준호") String director,
                                  @RequestParam(defaultValue = "3") int count) {
        return movieService.findMoviesByDirector(director, count);
    }

    @GetMapping("/raw-json-trap")
    public String rawJsonTrap(@RequestParam(defaultValue = "기생충") String title) {
        return movieService.askRawJsonTrap(title);
    }

    @GetMapping("/raw-json-fixed")
    public String rawJsonFixed(@RequestParam(defaultValue = "기생충") String title) {
        return movieService.askRawJsonFixed(title);
    }

    /** [S8~S10 노트] entity()가 자동 주입하는 포맷 지시문(JSON 스키마) 확인 */
    @GetMapping("/format")
    public String format() {
        return movieService.describeFormat();
    }

	@PostMapping("/review/llm")
	public ReviewAnalysis reviewLlm(@RequestBody String review) {
		return movieService.analyzeReviewWithLlm(review);
	}

	@PostMapping("/review/jev")
	public ReviewAnalysis reviewJev(@RequestBody String review) {
		if (!jevClient.enabled()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "jev.enabled=false");
		}
		return movieService.analyzeReviewWithJev(review);
	}
}

