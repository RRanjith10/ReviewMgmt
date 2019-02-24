/**
 * 
 */
package com.mindtree.review.management.repository;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mindtree.review.management.model.Review;

/**
 * @author Ranjith Ranganathan
 *
 */
@Repository
public interface ReviewManagementRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantId(String restaurantId, Pageable pageable);
    
    List<Review> findByRestaurantId(String restaurantId);
    
    List<Review> findByCustomerEmail(String custEmail, Pageable pageable);

    List<Review> findByCustomerEmail(String custEmail);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE review_rating r SET r.rating = :rating, r.comments = :comments, r.reviewed_date = :reviewedDate WHERE r.customer_email = :customerEmail AND r.restaurant_id = :restaurantId")
    int updateReviewById(@Param("restaurantId") String restaurantId, @Param("comments") String comments,
        @Param("reviewedDate") Date reviewedDate, @Param("customerEmail") String customerEmail,
        @Param("rating") float rating);
    
    Review findByRestaurantIdInAndCustomerEmail(String restaurantId, String customerEmail);
    
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE review_rating r SET r.like_count = like_count + 1 where r.review_id = :reviewId")
    int updateLikeCount(@Param("reviewId") Long reviewId);
    

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE review_rating r SET r.dislike_count = dislike_count + 1 where r.review_id = :reviewId")
    int updateDislikeCount(@Param("reviewId") Long reviewId);
    
    @Transactional
    @Query(nativeQuery = true, value = "SELECT r.like_count FROM review_rating r where r.review_id = :reviewId")
    int getLikeCount(@Param("reviewId") Long reviewId);
    
    @Transactional
    @Query(nativeQuery = true, value = "SELECT r.dislike_count FROM review_rating r where r.review_id = :reviewId")
    int getDislikeCount(@Param("reviewId") Long reviewId);
}
