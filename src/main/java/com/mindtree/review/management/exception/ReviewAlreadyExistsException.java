/**
 * 
 */
package com.mindtree.review.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author M1032466
 *
 */
@ResponseStatus(code = HttpStatus.CONFLICT)
public class ReviewAlreadyExistsException extends RuntimeException {

    /**
     * @param message
     */
    public ReviewAlreadyExistsException(String message) {
        super(message);
    }

}
