package com.mycompany.gamestatapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author Strix89
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Risposta 500 Internal Server Error
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}