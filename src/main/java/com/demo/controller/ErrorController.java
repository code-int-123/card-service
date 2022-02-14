package com.demo.controller;

import com.demo.generated.api.model.Error;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorController {
    @ExceptionHandler(value=IllegalStateException.class)
    public ResponseEntity<Error> illegalStateException(IllegalStateException exception){
        return ResponseEntity.status(400).body(new Error().info("BUSINESS").message(exception.getMessage()));
    }

    @ExceptionHandler(value=Exception.class)
    public ResponseEntity<Error> illegalStateException(Exception exception){
        return ResponseEntity.status(500).body(new Error().info("TECHNICAL").message(exception.getMessage()));
    }
}
