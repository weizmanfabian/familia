package com.weiz.Familia.api.controllers.errorHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.weiz.Familia.api.responses.errors.BaseErrorResponse;
import com.weiz.Familia.api.responses.errors.ErrorResponse;
import com.weiz.Familia.api.responses.errors.ErrorsResponse;
import com.weiz.Familia.util.Exceptions.CustomException;
import com.weiz.Familia.util.Exceptions.IdNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class BadRequestController {

    @ExceptionHandler(CustomException.class)
    public BaseErrorResponse handleCustomException(CustomException exception, HttpServletRequest request) {
        return ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.BAD_REQUEST.name())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(IdNotFoundException.class)
    public BaseErrorResponse handleIdNotFoundException(IdNotFoundException exception, HttpServletRequest request) {
        return ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND.name())
                .code(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
    }

    /**
     * Handles MethodArgumentNotValidException and constructs an ErrorsResponse containing
     * validation errors with their respective messages.
     *
     * @param exception the MethodArgumentNotValidException thrown when validation on an argument fails
     * @return an ErrorsResponse with details about the validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Add each validation error to the errors map.
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ErrorsResponse.builder()
                .errors(errors)
                .status(HttpStatus.BAD_REQUEST.name())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseErrorResponse handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        Throwable rootCause = ex.getRootCause();

        if (rootCause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) rootCause;

            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                Class<Enum> enumType = (Class<Enum>) ife.getTargetType();
                String inputValue = ife.getValue().toString();
                String fieldName = ife.getPath().get(0).getFieldName();

                Map<String, String> errors = new HashMap<>();
                errors.put(fieldName, "Valor inválido para este campo, verifique los valores permitidos");


                return ErrorsResponse.builder()
                        .errors(errors)
                        .status(HttpStatus.BAD_REQUEST.name())
                        .code(HttpStatus.BAD_REQUEST.value())
                        .timestamp(LocalDateTime.now().toString())
                        .path(request.getRequestURI())
                        .build();
            }
        }

        // Manejo genérico para otros errores de deserialización
        return ErrorResponse.builder()
                .message("Error en el formato de la solicitud")
                .status(HttpStatus.BAD_REQUEST.name())
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();
    }
}
