/**
 * 
 */
package com.mindtree.review.management.exception;

import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Ranjith Ranganathan
 *
 */
@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    
    @Value("${review.input.mismatch}")
    public String inputMismatch;
    
    @ExceptionHandler(ReviewNotFoundException.class)
    public final ResponseEntity<java.lang.Object> handleReviewNotFoundException(java.lang.Exception ex,
        WebRequest request) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), httpStatus.value(),
            httpStatus.getReasonPhrase(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(exceptionResponse, httpStatus);
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public final ResponseEntity<java.lang.Object> handleReviewAlreadyFoundException(java.lang.Exception ex,
        WebRequest request) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), httpStatus.value(),
            httpStatus.getReasonPhrase(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(exceptionResponse, httpStatus);
    }
    
    @ExceptionHandler(InvalidRestaurantIdFormatException.class)
    public final ResponseEntity<java.lang.Object> handleInvalidRestaurantIdFormatException(java.lang.Exception ex,
        WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), httpStatus.value(),
            httpStatus.getReasonPhrase(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(exceptionResponse, httpStatus);
    }

    @ExceptionHandler(NumberFormatException.class)
    public final ResponseEntity<java.lang.Object> handleNumberFormatException(NumberFormatException ex,
        WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), httpStatus.value(),
            httpStatus.getReasonPhrase(), inputMismatch, request.getDescription(false));
        return new ResponseEntity<>(exceptionResponse, httpStatus);
    }
    
    @ExceptionHandler(InputMismatchException.class)
    public final ResponseEntity<java.lang.Object> handleInputMisMatchException(NumberFormatException ex,
        WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), httpStatus.value(),
            httpStatus.getReasonPhrase(), inputMismatch, request.getDescription(false));
        return new ResponseEntity<>(exceptionResponse, httpStatus);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatus status, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        Map<String, String> errorMap = new HashMap<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), httpStatus.value(),
            httpStatus.getReasonPhrase(), inputMismatch,
            errorMap.toString());
        return new ResponseEntity<>(exceptionResponse, httpStatus);
    }

}
