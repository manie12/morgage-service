package io.bank.mortgage.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VersionConflictException extends RuntimeException {
    public VersionConflictException() {
        super("Entity has been modified by another transaction");
    }

    public VersionConflictException(String message) {
        super(message);
    }

    public VersionConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
