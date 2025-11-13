package com.example.bankcards.exception;

import com.example.bankcards.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Advice для централизованной обработки исключений и возвращения ошибок по кодам
 */
@RestControllerAdvice
public class ExceptionHandlingAdvice {


    @ExceptionHandler({IllegalArgumentException.class,
            MethodArgumentNotValidException.class, InvalidCardNumberException.class,
            EmailAlreadyExistsException.class, UsernameAlreadyExistsException.class,
            CardExpiredException.class, CardBlockedException.class,
            CardHasBalanceException.class, InvalidAmountException.class,
            InsufficientFundsException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class, UserNotFoundException.class,
            CardNotFoundException.class, TransactionNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(BadCredentialsException e) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler({AccessDeniedException.class, UnauthorizedTransferException.class,
    UnauthorizedStatusChangeException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException e) {
        return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception e) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server error - " + e.getMessage());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(message));
    }
}
