package hu.informula.demo_project.service;

import hu.informula.demo_project.entity.SearchPattern;
import hu.informula.demo_project.enums.ApiNameEnum;
import hu.informula.demo_project.repository.SearchPatternRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchPatternServiceTest {

    @Mock
    private SearchPatternRepository searchPatternRepository;

    @InjectMocks
    private SearchPatternService searchPatternService;

    @BeforeEach
    void setUp() {
        searchPatternService = new SearchPatternService(searchPatternRepository);
    }

    @Test
    void testSaveSearchPatternService_SavesCorrectly() {

        String title = "Inception";
        ApiNameEnum apiName = ApiNameEnum.OMDB;

        searchPatternService.saveSearchPatternService(title, apiName);

        verify(searchPatternRepository, times(1)).save(any(SearchPattern.class));
    }
}