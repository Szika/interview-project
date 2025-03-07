package hu.informula.demo_project.controller;

import hu.informula.demo_project.dto.MovieResponseDTO;
import hu.informula.demo_project.dto.MoviesResponseDTO;
import hu.informula.demo_project.enums.ApiNameEnum;
import hu.informula.demo_project.service.SearchMovieService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@WebFluxTest(MovieController.class)
public class MovieControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SearchMovieService searchMovieService;

    @Test
    void searchMovieByTitleAndApiName() {
        MovieResponseDTO movie = new MovieResponseDTO("Inception", "2010", List.of("Christopher Nolan"));
        MoviesResponseDTO responseDTO = new MoviesResponseDTO(List.of(movie));

        String title = "Inception";


        Mockito.when(searchMovieService.getMoviesByTitleAndApiName(title, ApiNameEnum.TMDB))
                .thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movies/"+title)
                        .queryParam("api", "TMDB")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.movies[0].Title").isEqualTo("Inception")
                .jsonPath("$.movies[0].Year").isEqualTo("2010")
                .jsonPath("$.movies[0].Director[0]").isEqualTo("Christopher Nolan");

        verify(searchMovieService, times(1)).getMoviesByTitleAndApiName(title, ApiNameEnum.TMDB);
        verify(searchMovieService, times(1)).getMoviesByTitleAndApiName(eq(title), eq(ApiNameEnum.TMDB));
    }

    @Test
    void searchMovieByTitleAndApiName_InvalidApi_ShouldReturnBadRequest() {
        String movieTitle = "Inception";
        String invalidApi = "INVALID_API";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movies/{movieTitle}")
                        .queryParam("api", invalidApi)
                        .build(movieTitle))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").exists();

        verify(searchMovieService, never()).getMoviesByTitleAndApiName(anyString(), any());
    }

}
