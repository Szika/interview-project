package hu.informula.demo_project.controller;

import hu.informula.demo_project.dto.MoviesResponseDTO;
import hu.informula.demo_project.enums.ApiNameEnum;
import hu.informula.demo_project.service.SearchMovieService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/movies")
@Log4j2
public class MovieController {

    @Autowired
    SearchMovieService searchMovieService;

    @GetMapping("/{movieTitle}")
    public ResponseEntity<Mono<MoviesResponseDTO>> searchMovieByTitleAndApiName(
            @PathVariable( "movieTitle" ) final String movieTitle ,
            @RequestParam ApiNameEnum api
    ) {
        Mono<MoviesResponseDTO> movies = searchMovieService.getMoviesByTitleAndApiName(movieTitle, api);
        return ResponseEntity.ok(movies);
    }

}
