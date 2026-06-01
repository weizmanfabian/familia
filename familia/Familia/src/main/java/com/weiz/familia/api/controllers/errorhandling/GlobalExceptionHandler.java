package com.weiz.familia.api.controllers.errorhandling;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.weiz.familia.api.responses.errors.BaseErrorResponse;
import com.weiz.familia.api.responses.errors.ErrorResponse;
import com.weiz.familia.api.responses.errors.ErrorsResponse;
import com.weiz.familia.shared.exceptions.CustomException;
import com.weiz.familia.shared.exceptions.RegistroNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MENSAJE_VALOR_ENUM_INVALIDO =
            "Valor inválido para este campo, verifique los valores permitidos";
    private static final String MENSAJE_FORMATO_INVALIDO =
            "Error en el formato de la solicitud";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseErrorResponse> handleCustomException(CustomException exception, HttpServletRequest request) {
        return construirErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(RegistroNoEncontradoException.class)
    public ResponseEntity<BaseErrorResponse> handleRegistroNoEncontrado(RegistroNoEncontradoException exception, HttpServletRequest request) {
        return construirErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return construirErroresResponse(errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        if (exception.getRootCause() instanceof InvalidFormatException ife
                && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            String fieldName = ife.getPath().get(0).getFieldName();
            Map<String, String> errors = Map.of(fieldName, MENSAJE_VALOR_ENUM_INVALIDO);
            return construirErroresResponse(errors, request);
        }

        return construirErrorResponse(MENSAJE_FORMATO_INVALIDO, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<BaseErrorResponse> construirErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        BaseErrorResponse body = ErrorResponse.builder()
                .message(message)
                .status(status.name())
                .code(status.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<BaseErrorResponse> construirErroresResponse(Map<String, String> errors, HttpServletRequest request) {
        BaseErrorResponse body = ErrorsResponse.builder()
                .errors(errors)
                .status(HttpStatus.BAD_REQUEST.name())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
