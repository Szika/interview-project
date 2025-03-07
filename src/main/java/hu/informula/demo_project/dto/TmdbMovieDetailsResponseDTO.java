package hu.informula.demo_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TmdbMovieDetailsResponseDTO {

    private int id;

    @JsonProperty("cast")
    private List<TmdbCrewMemberDTO> castMembers;

    @JsonProperty("crew")
    private List<TmdbCrewMemberDTO> crewMembers;

}
