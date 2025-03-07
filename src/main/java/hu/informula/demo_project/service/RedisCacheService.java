package hu.informula.demo_project.service;

import hu.informula.demo_project.dto.MoviesResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisCacheService{

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveToCache(MoviesResponseDTO data, String cacheKey) {
        redisTemplate.opsForValue().set(cacheKey, data, Duration.ofHours(24));
    }

    public MoviesResponseDTO getFromCache(String cacheKey) {
        return (MoviesResponseDTO) redisTemplate.opsForValue().get(cacheKey);
    }

}
