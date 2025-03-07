package hu.informula.demo_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDTO {

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Director")
    private List<String> director;

}
