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
import java.util.List;
import java.util.stream.Collectors;

@Service("TMDB")
@RequiredArgsConstructor
@Slf4j
public class TmdbMovieService implements MovieService {

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    private final WebClient tmdbWebClient;

    private final RedisCacheService redisCacheService;

    @Override
    public Mono<MoviesResponseDTO> getMovieDatas(String title) {

        String cacheKey = ApiNameEnum.TMDB + ":" + title;

        return searchForMovies(title)
                .flatMap(response -> {
                    if (response.getResults().isEmpty()) {
                        return Mono.just(new MoviesResponseDTO(new ArrayList<>()));
                    }
                    return getMovieData(response.getResults());
                })
                .doOnSuccess(response->redisCacheService.saveToCache(response, cacheKey));
    }

    private Mono<TmdbSearchResponseDTO> searchForMovies(String title) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", title)
                        .queryParam("api_key", tmdbApiKey)
                        .queryParam("include_adult", true)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponseDTO.class);
    }

    private Mono<MoviesResponseDTO> getMovieData(List<TmdbMovieResponseDTO> moviesList) {
        return Flux.fromIterable(moviesList)
                .flatMap(movie -> fetchMovieDetailsByImdbId(movie.getId().toString())
                        .map(details -> parseDatasToMovieResponseDTO(movie, details))
                )
                .collectList()
                .flatMap(movies-> Mono.just(new MoviesResponseDTO(movies)));
    }

    private Mono<TmdbMovieDetailsResponseDTO> fetchMovieDetailsByImdbId(String movieId) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/" + movieId + "/credits")
                        .queryParam("api_key", tmdbApiKey)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieDetailsResponseDTO.class);
    }

    private MovieResponseDTO parseDatasToMovieResponseDTO(TmdbMovieResponseDTO movie, TmdbMovieDetailsResponseDTO details) {
        MovieResponseDTO responseDTO = new MovieResponseDTO();
        responseDTO.setTitle(movie.getTitle());
        if(movie.getYear() != null) responseDTO.setYear(movie.getYear().toString());
        responseDTO.setDirector(details.getCrewMembers().stream().filter(cm->"Director".equals(cm.getJob())).map(TmdbCrewMemberDTO::getName).collect(Collectors.toList()));

        return responseDTO;
    }

}
