package com.enerlytics.devices.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERRORS_BASE = "/errors/";
    private static final String VALIDATION_TYPE = ERRORS_BASE + "validation";

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ProblemDetail> handleApplication(ApplicationException ex, HttpServletRequest request) {
        HttpStatus status = ex.getHttpStatus();
        log.warn("Application exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        ProblemDetail problem =
                buildProblemDetail(status, ex.getClass().getSimpleName(), ex.getMessage(), typeUriFor(status), request);
        return ResponseEntity.status(status).body(problem);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> String.valueOf(error.getDefaultMessage()),
                        (first, ignored) -> first));
        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Validation failed for request body",
                VALIDATION_TYPE,
                extractRequestUri(request));
        problemDetail.setProperty("errors", fieldErrors);
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    private ProblemDetail buildProblemDetail(
            HttpStatus status, String title, String detail, String typeUri, HttpServletRequest request) {
        return buildProblemDetail(status, title, detail, typeUri, request.getRequestURI());
    }

    private ProblemDetail buildProblemDetail(
            HttpStatus status, String title, String detail, String typeUri, String instance) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create(typeUri));
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(instance));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    private String typeUriFor(HttpStatus status) {
        return ERRORS_BASE + status.getReasonPhrase().toLowerCase().replace(' ', '-');
    }

    private String extractRequestUri(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return "unknown";
    }
}
