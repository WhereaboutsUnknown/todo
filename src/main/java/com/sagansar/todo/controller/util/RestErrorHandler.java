package com.sagansar.todo.controller.util;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.UserBlockedException;
import com.sagansar.todo.infrastructure.exceptions.WarningException;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class RestErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    protected @NonNull ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        ex.printStackTrace();
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        RestError error =
                new RestError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(
                ex, error, headers, error.getStatus(), request);
    }

    @Override
    protected @NonNull ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        ex.printStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(
                " method is not supported for this request. Supported methods are ");
        Objects.requireNonNull(ex.getSupportedHttpMethods()).forEach(t -> builder.append(t).append(" "));

        RestError apiError = new RestError(HttpStatus.METHOD_NOT_ALLOWED,
                ex.getLocalizedMessage(), builder.toString());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected @NonNull ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        ex.printStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        RestError error = new RestError(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ex.getLocalizedMessage(), builder.substring(0, builder.length() - 2));
        return new ResponseEntity<>(
                error, new HttpHeaders(), error.getStatus());
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        ex.printStackTrace();
        String errorMessage =
                ex.getName() + " should be of type " + Objects.requireNonNull(ex.getRequiredType()).getName();

        RestError error =
                new RestError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errorMessage);
        return new ResponseEntity<>(
                error, new HttpHeaders(), error.getStatus());
    }

    @ExceptionHandler({ WarningException.class })
    public ResponseEntity<Object> handleWarning(WarningException ex, WebRequest request) {
        ex.printStackTrace();
        RestWarning warning =
                new RestWarning(ex.getResponseMessage(), ex.getResponse(), ex.getAddition());
        return new ResponseEntity<>(
                warning, new HttpHeaders(), warning.getStatus());
    }

    @ExceptionHandler({ BadRequestException.class })
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex, WebRequest request) {
        ex.printStackTrace();
        RestError error =
                new RestError(HttpStatus.OK, ex.getResponseMessage(), "400");
        return new ResponseEntity<>(
                error, new HttpHeaders(), error.getStatus());
    }

    @ExceptionHandler({UserBlockedException.class})
    public ResponseEntity<Object> handleUserBlocked(UserBlockedException ex, WebRequest request) {
        ex.printStackTrace();
        RestError error =
                new RestError(HttpStatus.FORBIDDEN, ex.getResponseMessage(), "403");
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.OK);
    }

    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        ex.printStackTrace();
        throw ex;
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        ex.printStackTrace();
        RestError error = new RestError(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), "error occurred");
        return new ResponseEntity<>(
                error, new HttpHeaders(), error.getStatus());
    }
}
