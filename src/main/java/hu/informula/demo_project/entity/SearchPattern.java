package hu.informula.demo_project.entity;


import hu.informula.demo_project.enums.ApiNameEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity()
@Table(name="SEARCH_PATTERN")
public class SearchPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEARCH_PATTERN_ID_SEQ_GEN")
    @SequenceGenerator(name="SEARCH_PATTERN_ID_SEQ_GEN", sequenceName = "SEARCH_PATTERN_ID_SEQ")
    private Long id;

    @Column( name = "MOVIE_TITLE")
    private String movieTitle;

    @Column( name = "API_NAME")
    private String apiName;

    @Column( name = "DATE_TIME")
    private LocalDateTime dateTime;

    public SearchPattern(String movieTitle, ApiNameEnum apiName) {
        this.movieTitle = movieTitle;
        this.apiName = apiName.toString();
        this.dateTime = LocalDateTime.now();
    }
}
