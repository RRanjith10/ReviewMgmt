/**
 * 
 */
package com.mindtree.review.management.service;

import java.util.List;

import com.mindtree.review.management.model.Review;

/**
 * @author Ranjith Ranganathan
 *
 */

public interface ReviewManagementService {
    
    public List<Review> getReviewByRestaurantId(String restaurantId, Integer pageNumber, Integer count);
    
    public List<Review> getReviewByCustomerEmail(String custEmail, Integer pageNumber, Integer count);
    
    public Review addNewReview(Review review, String custEmail);
    
    public Review getReviewById(Long reviewId);
   
    public boolean removeReview(String restaurantId, String email);
    
    public Review updateReview(Review review, String custEmail);
    
    public int updateLikeCount(Long reviewId);
    
    public int updateDisLikeCount(Long reviewId);

}
