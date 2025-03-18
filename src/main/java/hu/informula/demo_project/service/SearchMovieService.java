package hu.informula.demo_project.service;

import hu.informula.demo_project.dto.*;
import hu.informula.demo_project.enums.ApiNameEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchMovieService {

    private final RedisCacheService redisCacheService;

    private final SearchPatternService searchPatternService;

    private final Map<String, MovieService> movieServiceMap;

    public Mono<MoviesResponseDTO> getMoviesByTitleAndApiName(String movieTitle, ApiNameEnum apiName) {

        searchPatternService.saveSearchPatternService(movieTitle, apiName);

        return getCompleteMovieDataWithCache(movieTitle, apiName);
    }

    @Cacheable(cacheNames = "movies", key = "#apiName + '_' + #title")
    public Mono<MoviesResponseDTO> getCompleteMovieDataWithCache(String title, ApiNameEnum apiName) {
        String cachekKey = apiName + ":" + title;
        MoviesResponseDTO cachedData = redisCacheService.getFromCache(cachekKey);

        if (cachedData != null) {
            return Mono.just(cachedData);
        }

        MovieService service = movieServiceMap.get(apiName.toString());
        if (service == null) {
            throw new IllegalArgumentException("Invalid API name: " + apiName);
        }

        return service.getMovieDatas(title);

    }

}
