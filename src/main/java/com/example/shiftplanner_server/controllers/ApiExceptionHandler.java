package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.ErrorReason;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorReason> handleResponseStatusException(ResponseStatusException ex) {
        if (!HttpStatus.BAD_REQUEST.equals(ex.getStatusCode())) {
            throw ex;
        }
        String reason = ex.getReason() == null ? "Bad Request" : ex.getReason();
        return ResponseEntity.badRequest().body(new ErrorReason().reason(reason));
    }
}

