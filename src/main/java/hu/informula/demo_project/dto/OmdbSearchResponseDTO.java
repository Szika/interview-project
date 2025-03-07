package hu.informula.demo_project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbSearchResponseDTO {

    @JsonProperty("Search")
    private List<OmdbMovieResponseDTO> search;

    @JsonProperty("totalResults")
    private String totalResults;

    @JsonProperty("Response")
    private String response;

    @JsonProperty("Error")
    private String error;

}
