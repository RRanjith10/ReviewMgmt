/**
 * 
 */
package com.mindtree.review.management.exception;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author M1032466
 *
 */
@Data
@AllArgsConstructor
public class ExceptionResponse {

    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String details;
}
