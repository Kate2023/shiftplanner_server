package com.example.shiftplanner_server.controllers;

import com.example.shiftplanner_server.model.ErrorReason;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void badRequestReasonIsReturnedInBody() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed");

        ResponseEntity<ErrorReason> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getReason());
    }

    @Test
    void badRequestWithoutReasonDefaultsToBadRequestMessage() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, null);

        ResponseEntity<ErrorReason> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getReason());
    }

    @Test
    void nonBadRequestExceptionIsRethrown() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Missing");

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
            () -> handler.handleResponseStatusException(ex));

        assertSame(ex, thrown);
    }
}

