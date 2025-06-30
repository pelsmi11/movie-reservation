package com.hectormartinezmoreira.user_service.web.exeption;

import com.hectormartinezmoreira.user_service.domain.dto.errors.ApiError;
import com.hectormartinezmoreira.user_service.domain.exception.ErrorMessageException;
import com.hectormartinezmoreira.user_service.domain.exception.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the User Service application.
 * This class handles various types of exceptions and converts them into appropriate HTTP responses
 * with corresponding status codes and error messages.
 * 
 * <p>This handler provides centralized exception handling across all @RequestMapping methods.</p>
 * 
 * @author Hector Martinez
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles UserAlreadyExistsException that occurs when attempting to create a user that already exists.
     *
     * @param ex the UserAlreadyExistsException that was thrown
     * @return ResponseEntity containing an ApiError with the error message and HTTP 409 Conflict status
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ApiError apiError = new ApiError(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // Código de estado para conflictos
                .body(apiError);
    }

    /**
     * Handles validation exceptions that occur when request body validation fails.
     *
     * @param ex the MethodArgumentNotValidException that was thrown
     * @return ResponseEntity containing a map of field errors and HTTP 400 Bad Request status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        StringBuilder combinedMessage = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String errorMessage = error.getDefaultMessage();
            errors.put(error.getField(), errorMessage);
            combinedMessage.append(errorMessage).append(", ");
        });

        // Remover la última coma y espacio
        if (!combinedMessage.isEmpty()) {
            combinedMessage.setLength(combinedMessage.length() - 2);
        }

        errors.put("message", combinedMessage.toString());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }



    /**
     * Handles ErrorMessageException that occurs when an application error needs to be signaled to the client.
     *
     * @param ex the ErrorMessageException that was thrown
     * @return ResponseEntity containing an ApiError with the error message and the HTTP status code corresponding
     * to the exception
     */
    @ExceptionHandler(ErrorMessageException.class)
    public ResponseEntity<ApiError> handleErrorMessageException(ErrorMessageException ex) {
        ApiError apiError = new ApiError(ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(apiError);
    }

}
