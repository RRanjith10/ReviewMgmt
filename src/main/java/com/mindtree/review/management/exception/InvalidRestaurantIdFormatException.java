package com.mindtree.review.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidRestaurantIdFormatException extends RuntimeException {

    public InvalidRestaurantIdFormatException(String message) {
        super(message);
    }
}
