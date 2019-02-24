package com.mindtree.review.management.service.impl;

import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mindtree.review.management.model.Review;
import com.mindtree.review.management.repository.ReviewManagementRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReviewManagementServiceCacheImplTest {
    @Autowired
    private ReviewManagementServiceImpl service;
 
    @MockBean
    private ReviewManagementRepository repository;
    
    @Before
    public void setUp() {
        service.reviewNotFound = "No reviews found";
        service.reviewAlreadyExist = "You have already given review for this restarant";
        service.reviewMismatch = "Reviews mismatch";
    }

    @Test
    public void testGetReviewByRstIdForCaching() {
        service.setHazelcastCacheSwitch("TRUE");
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        service.reviewByRstIdMap().put("100", reviewList);
        Mockito.when(repository.findByRestaurantId(Mockito.any(String.class))).thenReturn(reviewList);
        List<Review> reviewByRestaurantId = service.getReviewByRestaurantId("100", null, null);
        Assert.assertEquals(1, reviewByRestaurantId.size());
        Assert.assertEquals(review.getComments(), reviewByRestaurantId.get(0).getComments());
        Assert.assertEquals(review.getRating(), reviewByRestaurantId.get(0).getRating());
    }
    
    @Test
    public void testReviewByCustomerId() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Mockito.when(repository.findByCustomerEmail("user@example.com", new PageRequest(1, 5)))
        .thenReturn(reviewList);
        service.reviewByCustEmailMap().put("user@example.com", reviewList);
        List<Review> reviewByCustomerEmail = service.getReviewByCustomerEmail("user@example.com", null, null);
        Assert.assertNotNull(reviewByCustomerEmail);
        Assert.assertEquals(1, reviewByCustomerEmail.size());
    }
    
    @Test
    public void testAddNewReviews() {
        Review review = buildNewReview("Comments", 1L, 4.5f, "user@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        //review.setReviewId(null);
        Mockito.when(repository.save(Mockito.any(Review.class))).thenReturn(review);
        service.reviewByCustEmailMap().put("user@example.com", reviewList);
        service.addNewReview(review, "user@example.com");
        ArgumentCaptor<Review> reviewArgument = ArgumentCaptor.forClass(Review.class);
        Mockito.verify(repository, Mockito.times(1)).save(reviewArgument.capture());
        Assert.assertEquals(review.getComments(), reviewArgument.getValue().getComments());
        Assert.assertEquals(review.getRestaurantId(), reviewArgument.getValue().getRestaurantId());
        Assert.assertEquals(review.getRating(), reviewArgument.getValue().getRating(), 2f);
    }
    @Test
    public void testAddNewReviewsWhenCacheisEmpty() {
        Review review = buildNewReview("Comments", 1L, 4.5f, "user2@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        //review.setReviewId(null);
        Mockito.when(repository.save(Mockito.any(Review.class))).thenReturn(review);
        service.reviewByCustEmailMap().put("user1@example.com", reviewList);
        service.addNewReview(review, "user2@example.com");
        String hazelcastCacheSwitch = service.getHazelcastCacheSwitch();
        ArgumentCaptor<Review> reviewArgument = ArgumentCaptor.forClass(Review.class);
        Mockito.verify(repository, Mockito.times(1)).save(reviewArgument.capture());
        Assert.assertEquals(review.getComments(), reviewArgument.getValue().getComments());
        Assert.assertEquals(review.getRestaurantId(), reviewArgument.getValue().getRestaurantId());
        Assert.assertEquals(review.getRating(), reviewArgument.getValue().getRating(), 2f);
        Assert.assertEquals("TRUE", hazelcastCacheSwitch);
    }
    
    @Test
    public void testGetReviewById() {
        Review review = buildNewReview("Comments", 1L, 4.5F, "user@example.com", "100");
        Mockito.when(repository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(review));
        service.reviewMap().put(1L, review);
        Review reviewById = service.getReviewById(1L);
        Assert.assertTrue(reviewById != null);
        Assert.assertTrue(reviewById.getReviewId().equals(review.getReviewId()));
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

}
