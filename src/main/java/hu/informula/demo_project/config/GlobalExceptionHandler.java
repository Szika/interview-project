package hu.informula.demo_project.config;

import hu.informula.demo_project.dto.ErrorResponse;
import hu.informula.demo_project.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleInvalidEnumValue(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String expectedValues = "Allowed values: OMDB, TMDB";

        String errorMessage = "Invalid value '" + invalidValue + "' for parameter '" + paramName + "'. " + expectedValues;
        log.error(errorMessage);

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(ApiException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

}
