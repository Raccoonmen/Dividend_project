package com.dayone.exception;

import com.dayone.model.ErrorResponse;
import com.dayone.type.ErrorCode;
import io.lettuce.core.RedisConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

import static com.dayone.type.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final ErrorCode INVALID_REQUEST = ErrorCode.INVALID_REQUEST;

    @ExceptionHandler(ScrapException.class)
    public ErrorResponse handleScrapException(ScrapException e) {
        log.error("{} is occurred.", e.getErrorCode());

        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage(), e.getHttpStatus().value());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException is occurred.", e);

        return new ErrorResponse(INVALID_REQUEST, INVALID_REQUEST.getDescription(),
                HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException is occurred.", e);

        return new ErrorResponse(INVALID_REQUEST, INVALID_REQUEST.getDescription(),
                HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException is occurred.", e);

        return new ErrorResponse(ACCESS_DENIED, ACCESS_DENIED.getDescription(),
                HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler(RedisConnectionException.class)
    public ErrorResponse handleRedisConnectionException(RedisConnectionException e) {
        log.error("RedisConnectionException is occurred.", e);

        return new ErrorResponse(FAIL_TO_CONNECT_REDIS_SERVER,
                FAIL_TO_CONNECT_REDIS_SERVER.getDescription(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e) {
        log.error("Exception is occurred.", e);

        return new ErrorResponse(
                INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR.getDescription(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
