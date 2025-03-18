package hu.informula.demo_project.service;

import hu.informula.demo_project.dto.MoviesResponseDTO;
import reactor.core.publisher.Mono;

public interface MovieService {

    Mono<MoviesResponseDTO> getMovieDatas(String title);

}
