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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchMovieService {

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    private final WebClient omdbWebClient;

    private final WebClient tmdbWebClient;

    private final RedisCacheService redisCacheService;

    private final SearchPatternService searchPatternService;

    public Mono<MoviesResponseDTO> getMoviesByTitleAndApiName(String movieTitle, ApiNameEnum apiName) {
        searchPatternService.saveSearchPatternService(movieTitle, apiName);

        return getCompleteMovieDataWithCache(movieTitle, apiName);
    }

    private Mono<MoviesResponseDTO> getCompleteMovieDataWithCache(String title, ApiNameEnum apiName) {
        String cachekKey = apiName + ":" + title;
        MoviesResponseDTO cachedData = redisCacheService.getFromCache(cachekKey);

        if (cachedData != null) {
            return Mono.just(cachedData);
        }

        return switch (apiName) {
            case OMDB -> getOmdbCompleteMovieDatas(title, cachekKey);
            case TMDB -> getTmdbCompleteMovieDatas(title, cachekKey);
        };


    }

    private Mono<MoviesResponseDTO> getOmdbCompleteMovieDatas(String title, String cacheKey) {

        return searchForMoviesOnOmdb(title)
                .flatMap(response -> {
                    if ("False".equalsIgnoreCase(response.getResponse())) {
                        return Mono.just(new MoviesResponseDTO(new ArrayList<>()));
                    }

                    return getMovieDataFromOmdb(response.getSearch());
                })
                .doOnSuccess(response->redisCacheService.saveToCache(response, cacheKey));
    }

    private Mono<OmdbSearchResponseDTO> searchForMoviesOnOmdb(String title) {
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
                .flatMap(movie -> fetchMovieDetailsFromOmdbByImdbId(movie.getImdbID())
                        .map(details -> parseDatasToMovieResponseDTOFromOmdb(movie, details))
                )
                .collectList()
                .flatMap(movies-> Mono.just(new MoviesResponseDTO(movies)));
    }

    private Mono<OmdbMovieDetailsResponseDTO> fetchMovieDetailsFromOmdbByImdbId(String imdbID) {
        return omdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("i", imdbID)
                        .queryParam("apikey", omdbApiKey)
                        .build())
                .retrieve()
                .bodyToMono(OmdbMovieDetailsResponseDTO.class);
    }

    private MovieResponseDTO parseDatasToMovieResponseDTOFromOmdb(OmdbMovieResponseDTO movie, OmdbMovieDetailsResponseDTO details) {
        MovieResponseDTO responseDTO = new MovieResponseDTO();
        responseDTO.setTitle(movie.getTitle());
        responseDTO.setYear(movie.getYear());

        responseDTO.setDirector(Arrays.stream(details.getDirector().split(", ")).toList());

        return responseDTO;
    }

    private Mono<MoviesResponseDTO> getTmdbCompleteMovieDatas(String title, String cacheKey) {

        return searchForMoviesOnTmdb(title)
                .flatMap(response -> {
                    if (response.getResults().isEmpty()) {
                        return Mono.just(new MoviesResponseDTO(new ArrayList<>()));
                    }
                    return getMovieDataFromTmdb(response.getResults());
                })
                .doOnSuccess(response->redisCacheService.saveToCache(response, cacheKey));
    }

    private Mono<TmdbSearchResponseDTO> searchForMoviesOnTmdb(String title) {
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

    private Mono<MoviesResponseDTO> getMovieDataFromTmdb(List<TmdbMovieResponseDTO> moviesList) {
        return Flux.fromIterable(moviesList)
                .flatMap(movie -> fetchMovieDetailsFromTmdbByImdbId(movie.getId().toString())
                        .map(details -> parseDatasToMovieResponseDTOFromTmdb(movie, details))
                )
                .collectList()
                .flatMap(movies-> Mono.just(new MoviesResponseDTO(movies)));
    }

    private Mono<TmdbMovieDetailsResponseDTO> fetchMovieDetailsFromTmdbByImdbId(String movieId) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/" + movieId + "/credits")
                        .queryParam("api_key", tmdbApiKey)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMovieDetailsResponseDTO.class);
    }

    private MovieResponseDTO parseDatasToMovieResponseDTOFromTmdb(TmdbMovieResponseDTO movie, TmdbMovieDetailsResponseDTO details) {
        MovieResponseDTO responseDTO = new MovieResponseDTO();
        responseDTO.setTitle(movie.getTitle());
        responseDTO.setYear(movie.getYear().toString());

        responseDTO.setDirector(details.getCrewMembers().stream().filter(cm->"Director".equals(cm.getJob())).map(TmdbCrewMemberDTO::getName).collect(Collectors.toList()));

        return responseDTO;
    }

}
