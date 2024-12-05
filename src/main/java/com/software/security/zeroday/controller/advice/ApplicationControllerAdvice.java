package com.software.security.zeroday.controller.advice;

import com.software.security.zeroday.dto.ErrorEntity;
import com.software.security.zeroday.service.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class ApplicationControllerAdvice {
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        AlreadyUsedException.class,
        ValidationCodeException.class,
        UsernameNotFoundException.class,
        NotYetEnabledException.class,
        LockedException.class,
        BadPasswordException.class,
        PostNotFoundException.class,
        InvalidFileException.class,
        ConstraintException.class,
        HttpMediaTypeNotSupportedException.class,
    })
    public @ResponseBody ErrorEntity handleBadRequestException(RuntimeException e) {
        log.warn(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(e.getMessage())
            .build();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorEntity handleValidationExceptions(MethodArgumentNotValidException e) {
        log.warn(String.valueOf(e));
        String message = e.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toList()
            .stream().findFirst()
            .orElse(e.getMessage());

        return ErrorEntity.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .build();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadCredentialsException.class})
    public @ResponseBody ErrorEntity handleBadCredentialsException(BadCredentialsException e) {
        log.warn(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Invalid email or password")
            .build();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public @ResponseBody ErrorEntity handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Invalid request format: " + e.getMessage())
            .build();
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler({AlreadyProcessedException.class})
    public @ResponseBody ErrorEntity handleAlreadyProcessedException(RuntimeException e) {
        log.warn(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.CONFLICT.value())
            .message(e.getMessage())
            .build();
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})
    public @ResponseBody ErrorEntity handleAccessDeniedException(AccessDeniedException e) {
        log.warn(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .message(e.getMessage())
            .build();
    }


    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
        SignatureException.class,
        MalformedJwtException.class,
        ExpiredJwtException.class
    })
    public @ResponseBody ErrorEntity handleSignatureExceptionException(RuntimeException e) {
        log.warn(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .message(e.getMessage())
            .build();
    }

    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public @ResponseBody ErrorEntity handleAccessDeniedException(MaxUploadSizeExceededException e) {
        log.warn(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
            .message("Uploaded file exceeds the allowed size: 5MB for images and 100MB for videos")
            .build();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class})
    public @ResponseBody ErrorEntity handleRuntimeException(RuntimeException e) {
        log.error(String.valueOf(e));
        return ErrorEntity.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Something went wrong")
            .build();
    }
}
