package hu.informula.demo_project.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TmdbSearchResponseDTO {
    @JsonProperty("page")
    private int page;

    @JsonProperty("results")
    private List<TmdbMovieResponseDTO> results;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("total_results")
    private int totalResults;
}