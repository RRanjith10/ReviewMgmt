/**
 * 
 */
package com.mindtree.review.management.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mindtree.review.management.dto.ReviewDTO;
import com.mindtree.review.management.model.OAuthUser;
import com.mindtree.review.management.model.Review;
import com.mindtree.review.management.service.impl.ReviewManagementServiceImpl;
import com.mindtree.review.management.validator.ReviewManagementValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Ranjith Ranganathan
 *
 */
@RefreshScope
@RestController
@RequestMapping("/review")
@Api(value = "Review Management Controller", produces = MediaType.APPLICATION_JSON_VALUE, tags = {
        "Review Management" }, description = "Api's for managing the reviews")
public class ReviewManagementController {

    @Autowired
    private ReviewManagementServiceImpl service;

    @Autowired
    private HttpSession httpSession;

    private static final String X_USER_INFO = "X_USER_INFO";

    @Value("${review.like.msg}")
    public String commentLiked;

    @Value("${review.dislike.msg}")
    public String commentDisLiked;
    
    private static final String DISLIKE = "dislike";
    
    private static final String LIKE = "like";

    @ApiOperation(value = "Api to add new review to a restaurant", response = Review.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Review created successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to add review"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 200, message = "Review added successfully") })
    @PostMapping
    public ResponseEntity<Object> addReview(
        @ApiParam(value = "Review Details", required = true) @Valid @RequestBody ReviewDTO review) {
        Review reviewEntity = buildReview(review, getEmail());
        reviewEntity.setLikeCount(0L);
        reviewEntity.setDislikeCount(0L);
        Review newReview = service.addNewReview(reviewEntity, getEmail());
        Link like = linkTo(methodOn(ReviewManagementController.class).likeReview(newReview.getReviewId()))
            .withRel(LIKE);
        Link disLikeRel = linkTo(methodOn(ReviewManagementController.class).unlikeReview(newReview.getReviewId()))
            .withRel(DISLIKE);
        Resource<Review> resource = new Resource<>(newReview, like, disLikeRel);
        return new ResponseEntity<Object>(resource, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Api to update a review given by Customer", response = Review.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Review updated successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to update review"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "Review you are trying to update is not available") })
    @PutMapping
    public ResponseEntity<Object> updateReview(
        @ApiParam(value = "Review Details", required = true) @Valid @RequestBody ReviewDTO review) {
        Review reviewById = service.getReviewById(Long.parseLong(review.getReviewId()));
        Review reviewEntity = buildReview(review, getEmail());
        reviewEntity.setReviewId(reviewById.getReviewId());
        service.validateReview(reviewById, review.getRestaurantId(), getEmail());
        Review updatedReview = service.updateReview(reviewEntity, getEmail());
        Link like = linkTo(methodOn(ReviewManagementController.class).likeReview(updatedReview.getReviewId()))
            .withRel(LIKE);
        Link disLikeRel = linkTo(methodOn(ReviewManagementController.class).unlikeReview(updatedReview.getReviewId()))
            .withRel(DISLIKE);
        Resource<Review> resource = new Resource<>(updatedReview, like, disLikeRel);
        return new ResponseEntity<Object>(resource, HttpStatus.OK);
    }

    @ApiOperation(value = "Api to delete a review given by Customer from review id", response = Review.class)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Review deleted successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to delete review"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "Review you are trying to delete is not available") })
    @DeleteMapping("/{restaurant_id}")
    public ResponseEntity<Object> deleteReview(
        @ApiParam(value = "Restaurant Id", required = true) @PathVariable("restaurant_id") String restaurantId) {
        service.removeReview(restaurantId, getEmail());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Api to retrieve all the reviews for all restaurants given by the customer using email", response = Review.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Retrieved all the available reviews successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to retrieve reviews"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "No reviews available to retrieve") })
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviewsByCustomerEmail(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
        @RequestParam(value = "count", required = false) Integer count) {
        return new ResponseEntity<List<Review>>(service.getReviewByCustomerEmail(getEmail(), pageNumber, count), HttpStatus.OK);
    }

    @ApiOperation(value = "Api to retrieve the reviews for the selected restaurant", response = Review.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved all the available reviews for a restaurant successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to retrieve reviews"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "No reviews available to retrieve") })
    @GetMapping("/{restaurant_id}")
    public ResponseEntity<List<Review>> getReviewsForRestaurant(
        @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
        @RequestParam(value = "count", required = false) Integer count,
        @ApiParam(value = "Restaurant Id", required = true) @PathVariable("restaurant_id") String restaurantId) {
        return new ResponseEntity<List<Review>>(service.getReviewByRestaurantId(restaurantId, pageNumber, count), HttpStatus.OK);
    }

    @ApiOperation(value = "Api to like a review comment given by the customer", response = Review.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Review comment liked successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to like reviews"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "No reviews available to retrieve") })
    @PutMapping("/{review_id}/like")
    public ResponseEntity<Object> likeReview(
        @ApiParam(value = "Review Id", required = true) @PathVariable("review_id") Long reviewId) {
        int updateLikeCount = service.updateLikeCount(reviewId);
        String message = "Number of likes for review with id "+reviewId+" is "+updateLikeCount;
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @ApiOperation(value = "Api to dislike a review comment given by the customer", response = Review.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Review comment disliked successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to dislike reviews"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "No reviews available to retrieve") })
    @PutMapping("/{review_id}/dislike")
    public ResponseEntity<Object> unlikeReview(
        @ApiParam(value = "Review Id", required = true) @PathVariable("review_id") Long reviewId) {
        int disLikeCount = service.updateDisLikeCount(reviewId);
        String message = "Number of dislikes for review with id "+reviewId+" is "+disLikeCount;
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    private Review buildReview(ReviewDTO reviewDTO, String custEmail) {
        Review review = new Review();
        review.setComments(reviewDTO.getComments().trim());
        review.setRating(Float.parseFloat(reviewDTO.getRating().trim()));
        review.setRestaurantId(reviewDTO.getRestaurantId().trim());
        review.setCustomerEmail(custEmail);
        review.setReviewedDate(new Date());
        return review;
    }

    private String getEmail() {
        ReviewManagementValidator validator = new ReviewManagementValidator();
        OAuthUser user = (OAuthUser) httpSession.getAttribute(X_USER_INFO);
        if (validator.isEmailPresent(user)) {
            return user.getFirebase().getIdentities().getEmail().get(0);
        }
        return "";
    }

}
