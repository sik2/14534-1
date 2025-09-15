package com.back.global.globalExceptionHandler;

import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException e) {
        return new ResponseEntity<>(
                new RsData<>(
                "404-1",
                "존재하지 않는 데이터에 접근했습니다."
            ),
            NOT_FOUND
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        message
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException e) {

        return new ResponseEntity<>(
                new RsData<>(
                        "400-1",
                        "요청 본문 형식이 올바르지 않습니다."
                ),
                BAD_REQUEST
        );
    }
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handle(ServiceException e) {
        return new ResponseEntity<>(
                e.getRsData(),
                BAD_REQUEST
        );
    }

}
