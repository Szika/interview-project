package hu.informula.demo_project.config;

import hu.informula.demo_project.exception.ApiException;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import io.netty.resolver.dns.DnsErrorCauseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

import java.net.UnknownHostException;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${omdb.api.base-url}")
    private String omdbBaseUrl;
    @Value("${tmdb.api.base-url}")
    private String tmdbBaseUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean(name = "omdbWebClient")
    public WebClient omdbWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(omdbBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .filter(errorHandler())
                .build();
    }

    @Bean(name = "tmdbWebClient")
    public WebClient tmdbWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(tmdbBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .filter(errorHandler())
                .build();
    }

    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            HttpStatusCode status = clientResponse.statusCode();
            if (status.is4xxClientError() || status.is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("Api Error: {}", errorBody);
                            if (status == HttpStatus.UNAUTHORIZED) {
                                return Mono.error(new ApiException("Unauthorized request", HttpStatus.UNAUTHORIZED));
                            } else if (status == HttpStatus.FORBIDDEN) {
                                return Mono.error(new ApiException("Access forbidden", HttpStatus.FORBIDDEN));
                            } else if (status == HttpStatus.NOT_FOUND) {
                                return Mono.error(new ApiException("Resource not found", HttpStatus.NOT_FOUND));
                            } else {
                                return Mono.error(new ApiException("Unexpected error: " + errorBody, status));
                            }
                        });
            }
            return Mono.just(clientResponse);
        }).andThen(ExchangeFilterFunction.ofRequestProcessor(request -> Mono.just(request)
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.error(new ApiException("HTTP Error: " + ex.getStatusCode(), ex.getStatusCode())))
                .onErrorResume(UnknownHostException.class, ex ->
                        Mono.error(new ApiException("Unknown host: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE)))
                .onErrorResume(ConnectTimeoutException.class, ex ->
                        Mono.error(new ApiException("Connection timed out", HttpStatus.GATEWAY_TIMEOUT)))
                .onErrorResume(ReadTimeoutException.class, ex ->
                        Mono.error(new ApiException("Read timeout occurred", HttpStatus.GATEWAY_TIMEOUT)))
                .onErrorResume(PrematureCloseException.class, ex ->
                        Mono.error(new ApiException("Connection closed unexpectedly", HttpStatus.BAD_GATEWAY)))
                .onErrorResume(DnsErrorCauseException.class, ex ->
                        Mono.error(new ApiException("DNS resolution failed: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE)))
                .onErrorResume(TimeoutException.class, ex ->
                        Mono.error(new ApiException("Request timed out", HttpStatus.REQUEST_TIMEOUT)))
                .onErrorResume(Exception.class, ex ->
                        Mono.error(new ApiException("Unexpected error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)))));
    }
}
