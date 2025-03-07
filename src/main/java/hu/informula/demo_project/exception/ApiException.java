package hu.informula.demo_project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatusCode status;

    public ApiException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }
}
