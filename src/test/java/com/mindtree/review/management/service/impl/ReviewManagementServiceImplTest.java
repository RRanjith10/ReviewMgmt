package com.mindtree.review.management.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mindtree.review.management.exception.InvalidRestaurantIdFormatException;
import com.mindtree.review.management.exception.ReviewAlreadyExistsException;
import com.mindtree.review.management.exception.ReviewNotFoundException;
import com.mindtree.review.management.model.Review;
import com.mindtree.review.management.repository.ReviewManagementRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReviewManagementServiceImplTest {
    
    @Autowired
    private ReviewManagementServiceImpl service;
 
    @MockBean
    private ReviewManagementRepository repository;
    
    @Before
    public void setUp() {
        service.reviewNotFound = "No reviews found";
        service.reviewAlreadyExist = "You have already given review for this restarant";
        service.reviewMismatch = "Reviews mismatch";
        service.setHazelcastCacheSwitch("FALSE");
        service.invalidRestaurantId = "Restaurant Id is not in proper format. Restaurant Id cannot be other than Numeric/Alphanumeric.";
    }
    
    @Test
    public void testAddNewReviews() {
        Review review = buildNewReview("Comments", 1L, 4.5f, "user@example.com", "100");
        Mockito.when(repository.save(Mockito.any(Review.class))).thenReturn(review);
        service.addNewReview(review, "user@example.com");
        ArgumentCaptor<Review> reviewArgument = ArgumentCaptor.forClass(Review.class);
        Mockito.verify(repository, Mockito.times(1)).save(reviewArgument.capture());
        Assert.assertEquals(review.getComments(), reviewArgument.getValue().getComments());
        Assert.assertEquals(review.getRestaurantId(), reviewArgument.getValue().getRestaurantId());
        Assert.assertEquals(review.getRating(), reviewArgument.getValue().getRating(), 2f);
    }
    
    @Test
    public void testAddNewReviewsWhenReviewisAlreadyAddedByCustomer() {
        Review review = buildNewReview("Comments", 1L, 4.5f, "user@example.com", "100");
        Mockito
            .when(repository.findByRestaurantIdInAndCustomerEmail(Mockito.any(String.class), Mockito.any(String.class)))
            .thenReturn(review);
        try {
            service.addNewReview(review, "user@example.com");
        }
        catch (ReviewAlreadyExistsException ex) {
            Assert.assertEquals("You have already given review for this restarant", ex.getMessage());
        }
    }

    @Test
    public void testAddNewReviewsForEmptyRestaurantId() {
        Review review = buildNewReview("Comments", 1L, 4.5f, "user@example.com", "");
        try {
            service.addNewReview(review, "user@example.com");
        }
        catch (InvalidRestaurantIdFormatException ex) {
            Assert.assertEquals("Restaurant Id is not in proper format. Restaurant Id cannot be other than Numeric/Alphanumeric.", ex.getMessage());
        }
    }
    @Test
    public void testAddNewReviewsForInvalidRestaurantId() {
        Review review = buildNewReview("Comments", 1L, 4.5f, "user@example.com", "aa-1");
        try {
            service.addNewReview(review, "user@example.com");
        }
        catch (InvalidRestaurantIdFormatException ex) {
            Assert.assertEquals("Restaurant Id is not in proper format. Restaurant Id cannot be other than Numeric/Alphanumeric.", ex.getMessage());
        }
    }
    @Test
    public void testAddNewReviewsForNegativeRestaurantId() {
        Review review = buildNewReview("Comments", 1L, 4.5f, "user@example.com", "0");
        try {
            service.addNewReview(review, "user@example.com");
        }
        catch (InvalidRestaurantIdFormatException ex) {
            Assert.assertEquals("Restaurant Id is not in proper format. Restaurant Id cannot be other than Numeric/Alphanumeric.", ex.getMessage());
        }
    }
    
    @Test
    public void testRemoveReview() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        Mockito.when(repository.findByRestaurantIdInAndCustomerEmail(Mockito.any(String.class), Mockito.any(String.class)))
        .thenReturn(review);
        service.removeReview("1", "user@example.com");
        Mockito.verify(repository, Mockito.times(1)).findByRestaurantIdInAndCustomerEmail(Mockito.any(String.class), Mockito.any(String.class));
        Mockito.verify(repository, Mockito.times(1)).delete(review);
    }
    
    @Test
    public void testRemoveReviewForInvalidId() {
        Mockito.when(repository.findByRestaurantIdInAndCustomerEmail(Mockito.any(String.class), Mockito.any(String.class)))
        .thenReturn(null);
        try {
            service.removeReview("12", "user@example.com");
        }
        catch (ReviewNotFoundException ex) {
            Assert.assertNotNull(ex.getMessage());
        }
    }
    
    @Test
    public void testGetAllReviewsWhenN() {
        List<Review> reviewList = new ArrayList<>();
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        try {
            reviewList.add(review);
        }
        catch (ReviewNotFoundException ex) {
            Assert.assertEquals("No Reviews found. Add few and check again", ex.getMessage());
        }
    }
    
    @Test
    public void testUpdateReview() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        Mockito.when(repository.updateReviewById(Mockito.any(String.class), Mockito.any(String.class),Mockito.any(Date.class), Mockito.any(String.class), Mockito.any(Float.class))).thenReturn(1);
        Review updateReview = service.updateReview(review, "user@example.com");
        Assert.assertNotNull(updateReview);
    }
    @Test
    public void testUpdateReviewForInvalidEmail() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        Mockito.when(repository.updateReviewById(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(Date.class), Mockito.any(String.class), Mockito.any(Float.class))).thenReturn(0);
        try {
            service.updateReview(review, "user@example.com");
        }
        catch(ReviewNotFoundException ex) {
            Assert.assertEquals("No reviews found", ex.getMessage());
        }
    }
    @Test
    public void testReviewByCustomerId() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Mockito.when(repository.findByCustomerEmail("user@example.com", new PageRequest(1, 5)))
        .thenReturn(reviewList);
        List<Review> reviewByCustomerEmail = service.getReviewByCustomerEmail("user@example.com", 1, 5);
        Assert.assertNotNull(reviewByCustomerEmail);
        Assert.assertEquals(1, reviewByCustomerEmail.size());
    }
    
    @Test
    public void testReviewByCustomerIdWithoutPagination() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Mockito.when(repository.findByCustomerEmail("user@example.com"))
        .thenReturn(reviewList);
        List<Review> reviewByCustomerEmail = service.getReviewByCustomerEmail("user@example.com", null, null);
        Assert.assertNotNull(reviewByCustomerEmail);
        Assert.assertEquals(1, reviewByCustomerEmail.size());
    }
    @Test
    public void testReviewByCustomerIdWithoutPaginationForInvalidEmail() {
        List<Review> reviewList = new ArrayList<>();
        Mockito.when(repository.findByCustomerEmail("user@example.com"))
        .thenReturn(reviewList);
        try{
            service.getReviewByCustomerEmail("user@example.com", null, null);
        }
        catch (ReviewNotFoundException ex) {
            Assert.assertEquals("No reviews found", ex.getMessage());
        }
    }
    
    @Test
    public void testReviewByCustomerIdForInvalidEmail() {
        Mockito.when(repository.findById(Mockito.anyLong()))
        .thenReturn(null);
        try {
            service.getReviewByCustomerEmail("user1@example.com", 1, 5);
        }
        catch (ReviewNotFoundException ex) {
            Assert.assertEquals("No reviews found", ex.getMessage());
        }
    }
    
    @Test
    public void testGetReviewByRestaurantIdWithoutPagination() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Mockito.when(repository.findByRestaurantId("100")).thenReturn(reviewList);
        List<Review> reviewByRestaurantId = service.getReviewByRestaurantId("100", null, null);
        Assert.assertNotNull(reviewByRestaurantId);
        Assert.assertEquals(1, reviewByRestaurantId.size());
    }
    
    @Test
    public void testGetReviewByRestaurantIdForInvalidIdWithoutPagination() {
        Mockito.when(repository.findByRestaurantId(Mockito.any(String.class))).thenReturn(null);
        try {
            service.getReviewByRestaurantId("1000", null, null);
        }
        catch(ReviewNotFoundException ex) {
            Assert.assertEquals("No reviews found", ex.getMessage());
        }
    }

    private Review buildNewReview(String comments, long reviewId, float rating, String customerEmail, String restaurantId){
        Review review = new Review();
        review.setComments(comments);
        review.setCustomerEmail(customerEmail);
        review.setRating(rating);
        review.setRestaurantId(restaurantId);
        review.setReviewId(reviewId);
        review.setReviewedDate(new Date());
        return review;
    }
    
    @Bean
    private MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        messageSource.setBasename("review-management-service");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
    
    @Test
    public void testGetReviewById() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        Mockito.when(repository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(review));
        Review reviewById = service.getReviewById(1L);
        Assert.assertTrue(reviewById != null);
        Assert.assertTrue(reviewById.getReviewId().equals(review.getReviewId()));
    }
    
    @Test
    public void testGetReviewByIdForInvalidId() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        Mockito.when(repository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(review));
        try {
            service.getReviewById(2L);
        }
        catch (ReviewNotFoundException ex) {
            Assert.assertEquals("No review found", ex.getMessage());
        }
    }
    
    @Test
    public void testUpdateLikeCount() {
    	Mockito.when(repository.updateLikeCount(Mockito.any(Long.class))).thenReturn(1);
    	Mockito.when(repository.getLikeCount(Mockito.any(Long.class))).thenReturn(1);
    	int updateLikeCount = service.updateLikeCount(1L);
    	Assert.assertEquals(1, updateLikeCount);
    }
    
    @Test
    public void testUpdateLikeCountForInvalidId() {
    	Mockito.when(repository.updateLikeCount(Mockito.any(Long.class))).thenReturn(0);
    	try {
    		service.updateLikeCount(1L);
    	}
    	catch (ReviewNotFoundException ex) {
            Assert.assertEquals("No reviews found", ex.getMessage());
        }
    }
    @Test
    public void testUpdateDisLikeCount() {
    	Mockito.when(repository.updateDislikeCount(Mockito.any(Long.class))).thenReturn(1);
    	Mockito.when(repository.getDislikeCount(Mockito.any(Long.class))).thenReturn(1);
    	int dislikeCount = service.updateDisLikeCount(1L);
    	Assert.assertEquals(1, dislikeCount);
    }
    
    @Test
    public void testUpdateDisLikeCountForInvalidId() {
    	Mockito.when(repository.updateDislikeCount(Mockito.any(Long.class))).thenReturn(0);
    	try {
    		service.updateDisLikeCount(1L);
    	}
    	catch (ReviewNotFoundException ex) {
            Assert.assertEquals("No reviews found", ex.getMessage());
        }
    }
    @Test
    public void testValidateReview() {
    	Review reviewEntity=buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
    	try{
    		service.validateReview(reviewEntity, "10", "user@example.com");
    	}
    	catch (InputMismatchException ex) {
            Assert.assertEquals("No reviews for the given restaurant id and customer email", ex.getMessage());
        }
    }
    @Test(expected=InputMismatchException.class)
    public void testValidateReviewForInvalidEmailId() {
    	Review reviewEntity=buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
    	service.validateReview(reviewEntity, "100", "user1@example.com");
    }
    @Test
	public void testValidateReviewForValidEmailId() {
		Review reviewEntity = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
		service.validateReview(reviewEntity, "100", "user@example.com");
	}
}
