package hu.informula.demo_project.service;

import hu.informula.demo_project.entity.SearchPattern;
import hu.informula.demo_project.enums.ApiNameEnum;
import hu.informula.demo_project.repository.SearchPatternRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class SearchPatternService {

    @Autowired
    SearchPatternRepository searchPatternRepository;

    public void saveSearchPatternService(String title, ApiNameEnum apiName) {
        SearchPattern searchPattern = new SearchPattern(title, apiName);
        this.searchPatternRepository.save( searchPattern );
    }
}
