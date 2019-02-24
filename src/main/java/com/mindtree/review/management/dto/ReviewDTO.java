package com.mindtree.review.management.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Range;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReviewDTO {

    private String reviewId;
    @NotNull(message = "Restuarant ID cannot be null")
    @Pattern(regexp = "^[a-zA-Z0-9-_]*$", message = "Restuarant ID cannot be empty or negative or zero or cannot have any special characters")
    private String restaurantId;

    @Pattern(regexp = "^[a-zA-Z0-9_.,' ]*$", message = "Comments cannot have Special characters. Only Alphabets are allowed")
    private String comments;

    @NotNull(message = "Rating cannot be null")
    @Range(min = 1, max = 5, message = "Rating should be within 1 to 5")
    private String rating;
}
