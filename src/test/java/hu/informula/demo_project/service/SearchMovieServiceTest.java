package hu.informula.demo_project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.informula.demo_project.dto.*;
import hu.informula.demo_project.enums.ApiNameEnum;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchMovieServiceTest {

    private WebClient omdbWebClient;

    private WebClient tmdbWebClient;

    private MockWebServer mockWebServer;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private SearchPatternService searchPatternService;

    @InjectMocks
    private SearchMovieService searchMovieService;

    @BeforeEach
    void setupMocks() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        omdbWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        tmdbWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        ReflectionTestUtils.setField(searchMovieService, "omdbWebClient", omdbWebClient);
        ReflectionTestUtils.setField(searchMovieService, "tmdbWebClient", tmdbWebClient);
    }

    @AfterEach
    void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetMoviesByTitleAndApiName_OmdbCacheHit() {
        String title = "Matrix";
        ApiNameEnum apiName = ApiNameEnum.OMDB;
        MoviesResponseDTO cachedResponse = new MoviesResponseDTO(List.of(
                new MovieResponseDTO("The Matrix", "1999", List.of("Lana Wachowski", "Lilly Wachowski"))
        ));

        when(redisCacheService.getFromCache(anyString())).thenReturn(cachedResponse);

        Mono<MoviesResponseDTO> responseMono = searchMovieService.getMoviesByTitleAndApiName(title, apiName);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.getMovies().getFirst().getTitle().equals("The Matrix"))
                .verifyComplete();

        verify(redisCacheService, never()).saveToCache(any(), anyString());
        verify(searchPatternService, times(1)).saveSearchPatternService(title, apiName);
    }

    @Test
    void testGetMoviesByTitleAndApiName_OmdbCacheMiss() throws JsonProcessingException {
        String title = "Matrix";
        ApiNameEnum apiName = ApiNameEnum.OMDB;

        ReflectionTestUtils.setField(searchMovieService, "omdbApiKey", "dummy-api-key");

        OmdbSearchResponseDTO searchResponse = new OmdbSearchResponseDTO();
        searchResponse.setResponse("True");
        searchResponse.setSearch(List.of(
                new OmdbMovieResponseDTO("The Matrix", "1999", "tt0133093", "movie", "https://test.te")
        ));

        OmdbMovieDetailsResponseDTO detailsResponse = new OmdbMovieDetailsResponseDTO();
        detailsResponse.setDirector("Lana Wachowski, Lilly Wachowski");

        ObjectMapper mapper = new ObjectMapper();
        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(searchResponse))
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(detailsResponse))
                .addHeader("Content-Type", "application/json"));

        when(redisCacheService.getFromCache(anyString())).thenReturn(null);

        Mono<MoviesResponseDTO> responseMono = searchMovieService.getMoviesByTitleAndApiName(title, apiName);

        StepVerifier.create(responseMono)
                .expectNextMatches(response ->
                        response.getMovies().size() == 1 &&
                                response.getMovies().getFirst().getDirector().size() == 2 &&
                                response.getMovies().getFirst().getTitle().equals("The Matrix")
                )
                .verifyComplete();

        verify(redisCacheService, times(1)).saveToCache(any(MoviesResponseDTO.class), anyString());
        verify(searchPatternService, times(1)).saveSearchPatternService(title, apiName);
    }

    @Test
    void testGetMoviesByTitleAndApiName_OmdbNoResults() throws JsonProcessingException {
        String title = "NonExistentMovie";
        ApiNameEnum apiName = ApiNameEnum.OMDB;

        ReflectionTestUtils.setField(searchMovieService, "omdbApiKey", "dummy-api-key");

        OmdbSearchResponseDTO searchResponse = new OmdbSearchResponseDTO();
        searchResponse.setResponse("False");
        searchResponse.setSearch(null);
        searchResponse.setError("Movie not found!");

        ObjectMapper mapper = new ObjectMapper();
        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(searchResponse))
                .addHeader("Content-Type", "application/json"));

        when(redisCacheService.getFromCache(anyString())).thenReturn(null);

        Mono<MoviesResponseDTO> responseMono = searchMovieService.getMoviesByTitleAndApiName(title, apiName);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.getMovies().isEmpty())
                .verifyComplete();

        verify(redisCacheService, times(1)).saveToCache(any(MoviesResponseDTO.class), anyString());
        verify(searchPatternService, times(1)).saveSearchPatternService(title, apiName);
    }

    @Test
    void testGetMoviesByTitleAndApiName_TmdbCacheHit() {
        String title = "Inception";
        ApiNameEnum apiName = ApiNameEnum.TMDB;
        MoviesResponseDTO cachedResponse = new MoviesResponseDTO(List.of(
                new MovieResponseDTO("Inception", "2010", List.of("Christopher Nolan"))
        ));

        when(redisCacheService.getFromCache(anyString())).thenReturn(cachedResponse);

        Mono<MoviesResponseDTO> responseMono = searchMovieService.getMoviesByTitleAndApiName(title, apiName);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.getMovies().getFirst().getTitle().equals("Inception"))
                .verifyComplete();

        verify(redisCacheService, never()).saveToCache(any(), anyString());
        verify(searchPatternService, times(1)).saveSearchPatternService(title, apiName);
    }

    @Test
    void testGetMoviesByTitleAndApiName_TmdbNoResults() throws JsonProcessingException {
        String title = "NonExistentMovie";
        ApiNameEnum apiName = ApiNameEnum.TMDB;

        ReflectionTestUtils.setField(searchMovieService, "tmdbApiKey", "dummy-api-key");

        TmdbSearchResponseDTO searchResponse = new TmdbSearchResponseDTO();
        searchResponse.setResults(List.of());

        ObjectMapper mapper = new ObjectMapper();
        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(searchResponse))
                .addHeader("Content-Type", "application/json"));

        when(redisCacheService.getFromCache(anyString())).thenReturn(null);

        Mono<MoviesResponseDTO> responseMono = searchMovieService.getMoviesByTitleAndApiName(title, apiName);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.getMovies().isEmpty())
                .verifyComplete();

        verify(redisCacheService, times(1)).saveToCache(any(MoviesResponseDTO.class), anyString());
        verify(searchPatternService, times(1)).saveSearchPatternService(title, apiName);
    }

    @Test
    void testGetMoviesByTitleAndApiName_TmdbCacheMiss() throws JsonProcessingException {
        String title = "Inception";
        ApiNameEnum apiName = ApiNameEnum.TMDB;

        ReflectionTestUtils.setField(searchMovieService, "tmdbApiKey", "dummy-api-key");

        TmdbSearchResponseDTO searchResponse = new TmdbSearchResponseDTO();

        TmdbMovieResponseDTO movie = new TmdbMovieResponseDTO();
        movie.setTitle(title);
        movie.setId(26811);
        movie.setReleaseDate(LocalDate.of(2010, 7, 15));

        searchResponse.setResults(List.of(
                movie
        ));

        TmdbMovieDetailsResponseDTO detailsResponse = new TmdbMovieDetailsResponseDTO();

        TmdbCrewMemberDTO crew1 = new TmdbCrewMemberDTO();
        crew1.setName("Christopher Nolan");
        crew1.setJob("Director");

        detailsResponse.setCrewMembers(List.of(
                crew1
        ));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(searchResponse))
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(detailsResponse))
                .addHeader("Content-Type", "application/json"));

        when(redisCacheService.getFromCache(anyString())).thenReturn(null);

        Mono<MoviesResponseDTO> responseMono = searchMovieService.getMoviesByTitleAndApiName(title, apiName);

        StepVerifier.create(responseMono)
                .expectNextMatches(response ->
                        response.getMovies().size() == 1 &&
                                response.getMovies().getFirst().getDirector().contains("Christopher Nolan") &&
                                response.getMovies().getFirst().getTitle().equals("Inception")
                )
                .verifyComplete();

        verify(redisCacheService, times(1)).saveToCache(any(MoviesResponseDTO.class), anyString());
        verify(searchPatternService, times(1)).saveSearchPatternService(title, apiName);
    }
}
