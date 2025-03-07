package hu.informula.demo_project.service;

import hu.informula.demo_project.dto.MoviesResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisCacheService redisCacheService;

    private final String cacheKey = "movie_avengers";
    private final MoviesResponseDTO mockResponse = new MoviesResponseDTO();

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSaveToCache() {
        redisCacheService.saveToCache(mockResponse, cacheKey);
        verify(valueOperations, times(1)).set(cacheKey, mockResponse, Duration.ofHours(24));
    }

    @Test
    void testGetFromCache_Found() {
        when(valueOperations.get(cacheKey)).thenReturn(mockResponse);

        MoviesResponseDTO cachedData = redisCacheService.getFromCache(cacheKey);

        assertNotNull(cachedData);
        assertEquals(mockResponse, cachedData);

        verify(valueOperations, times(1)).get(cacheKey);
    }

    @Test
    void testGetFromCache_NotFound() {
        when(valueOperations.get(cacheKey)).thenReturn(null);

        MoviesResponseDTO cachedData = redisCacheService.getFromCache(cacheKey);

        assertNull(cachedData);

        verify(valueOperations, times(1)).get(cacheKey);
    }
}