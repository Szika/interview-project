package hu.informula.demo_project.service;

import hu.informula.demo_project.dto.*;
import hu.informula.demo_project.enums.ApiNameEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("OMDB")
@RequiredArgsConstructor
@Slf4j
public class OmdbMovieService implements MovieService {

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    private final WebClient omdbWebClient;

    private final RedisCacheService redisCacheService;

    @Override
    public Mono<MoviesResponseDTO> getMovieDatas(String title) {

        String cacheKey = ApiNameEnum.TMDB + ":" + title;

        return searchForMovies(title)
                .flatMap(response -> {
                    if ("False".equalsIgnoreCase(response.getResponse())) {
                        return Mono.just(new MoviesResponseDTO(new ArrayList<>()));
                    }

                    return getMovieDataFromOmdb(response.getSearch());
                })
                .doOnSuccess(response->redisCacheService.saveToCache(response, cacheKey));
    }

    private Mono<OmdbSearchResponseDTO> searchForMovies(String title) {
        return omdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("s", title)
                        .queryParam("apikey", omdbApiKey)
                        .build())
                .retrieve()
                .bodyToMono(OmdbSearchResponseDTO.class);
    }

    private Mono<MoviesResponseDTO> getMovieDataFromOmdb(List<OmdbMovieResponseDTO> moviesList) {
        return Flux.fromIterable(moviesList)
                .flatMap(movie -> fetchMovieDetailsByImdbId(movie.getImdbID())
                        .map(details -> parseDatasToMovieResponseDTO(movie, details))
                )
                .collectList()
                .flatMap(movies-> Mono.just(new MoviesResponseDTO(movies)));
    }

    private Mono<OmdbMovieDetailsResponseDTO> fetchMovieDetailsByImdbId(String imdbID) {
        return omdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("i", imdbID)
                        .queryParam("apikey", omdbApiKey)
                        .build())
                .retrieve()
                .bodyToMono(OmdbMovieDetailsResponseDTO.class);
    }

    private MovieResponseDTO parseDatasToMovieResponseDTO(OmdbMovieResponseDTO movie, OmdbMovieDetailsResponseDTO details) {
        MovieResponseDTO responseDTO = new MovieResponseDTO();
        responseDTO.setTitle(movie.getTitle());
        responseDTO.setYear(movie.getYear());

        responseDTO.setDirector(Arrays.stream(details.getDirector().split(", ")).toList());

        return responseDTO;
    }

}
