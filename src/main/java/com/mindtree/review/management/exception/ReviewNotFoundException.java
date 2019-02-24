/**
 * 
 */
package com.mindtree.review.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ranjith Ranganathan
 *
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ReviewNotFoundException extends RuntimeException {

    /**
     * @param arg0
     */
    public ReviewNotFoundException(String arg0) {
        super(arg0);
    }
}
