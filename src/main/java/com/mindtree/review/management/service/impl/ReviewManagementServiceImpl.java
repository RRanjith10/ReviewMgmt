package com.mindtree.review.management.service.impl;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.mindtree.review.management.exception.InvalidRestaurantIdFormatException;
import com.mindtree.review.management.exception.ReviewAlreadyExistsException;
import com.mindtree.review.management.exception.ReviewNotFoundException;
import com.mindtree.review.management.model.Review;
import com.mindtree.review.management.repository.ReviewManagementRepository;
import com.mindtree.review.management.service.ReviewManagementService;

@Service
public class ReviewManagementServiceImpl implements ReviewManagementService {

	@Autowired
	private ReviewManagementRepository repository;

	@Autowired
	public HazelcastInstance hazelcastInstance;

	@Value("${review.already.exist}")
	public String reviewAlreadyExist;

	@Value("${review.not.found}")
	public String reviewNotFound;

	@Value("${review.mismatch}")
	public String reviewMismatch;

	@Value("${HAZELCAST.CACHE.SWITCH}")
	private String hazelcastCacheSwitch;
	
	@Value("${review.invalid.format}")
	public String invalidRestaurantId;

	public String getHazelcastCacheSwitch() {
		return hazelcastCacheSwitch;
	}

	public void setHazelcastCacheSwitch(final String hazelcastCacheSwitchParam) {
		this.hazelcastCacheSwitch = hazelcastCacheSwitchParam;
	}

	public IMap<Long, Review> reviewMap() {
		return hazelcastInstance.getMap("reviewbyid");
	}

	public IMap<String, List<Review>> reviewByCustEmailMap() {
		return hazelcastInstance.getMap("reviewbycustemail");
	}

	public IMap<String, List<Review>> reviewByRstIdMap() {
		return hazelcastInstance.getMap("reviewbyrstid");
	}

	@Override
    public Review addNewReview(Review review, String custEmail) {
        Review responseReview = null;
        if (isValidRestaurantId(review.getRestaurantId())) {
            Review reviewByEMail = repository.findByRestaurantIdInAndCustomerEmail(review.getRestaurantId(), custEmail);
            if (reviewByEMail == null) {
                responseReview = repository.save(review);
                updateCache(review);
            }
            else {
                throw new ReviewAlreadyExistsException(reviewAlreadyExist);
            }
        }
        else {
            throw new InvalidRestaurantIdFormatException(invalidRestaurantId);
        }
        return responseReview;
    }

    @Override
	public boolean removeReview(String restaurantId, String email) {
		boolean isRemoveSuccess = false;
		Review review = repository.findByRestaurantIdInAndCustomerEmail(restaurantId, email);
		if (review != null) {
			repository.delete(review);
			isRemoveSuccess = true;
			removeCacheValues(review);
		} else {
			throw new ReviewNotFoundException(reviewNotFound);
		}
		return isRemoveSuccess;
	}

	@Override
	public Review updateReview(Review review, String custEmail) {
		int update = repository.updateReviewById(review.getRestaurantId(), review.getComments(),
				review.getReviewedDate(), custEmail, review.getRating());
		if (update != 1) {
			throw new ReviewNotFoundException(reviewNotFound);
		}
		updateCacheValues(review);
		return review;
	}

	@Override
	public int updateLikeCount(Long reviewId) {
		int updateLikeCount = repository.updateLikeCount(reviewId);
		if (updateLikeCount != 1) {
			throw new ReviewNotFoundException(reviewNotFound);
		}
		return repository.getLikeCount(reviewId);
	}
	
	@Override
	public int updateDisLikeCount(Long reviewId) {
		int updateDislikeCount = repository.updateDislikeCount(reviewId);
		if (updateDislikeCount != 1) {
			throw new ReviewNotFoundException(reviewNotFound);
		}
		return repository.getDislikeCount(reviewId);
	}

	public void validateReview(Review reviewEntity, String restaurantId, String custEmail) {
		if (!(reviewEntity.getRestaurantId().equals(restaurantId))
				|| !(reviewEntity.getCustomerEmail().equals(custEmail))) {
			throw new InputMismatchException(reviewMismatch);
		}
	}

	@Override
    public List<Review> getReviewByRestaurantId(String restaurantId, Integer pageNumber, Integer count) {
        List<Review> reviewList = null;
        boolean isRequestParamPresent = false;
        if (pageNumber != null && count != null) {
            isRequestParamPresent = true;
        }
        if (isRequestParamPresent) {
            reviewList = repository.findByRestaurantId(restaurantId, new PageRequest(pageNumber, count));
            if (reviewList == null || reviewList.isEmpty()) {
                throw new ReviewNotFoundException(reviewNotFound);
            }
            reviewByRstIdMap().put(restaurantId, reviewList);
        }
        else if ("TRUE".equalsIgnoreCase(hazelcastCacheSwitch) && reviewByRstIdMap().containsKey(restaurantId)) {
            reviewList = reviewByRstIdMap().get(restaurantId);
        }
        else {
            reviewList = repository.findByRestaurantId(restaurantId);
            if (reviewList == null || reviewList.isEmpty()) {
                throw new ReviewNotFoundException(reviewNotFound);
            }
            reviewByRstIdMap().put(restaurantId, reviewList);
        }
        return reviewList;
    }

	@Override
	public Review getReviewById(Long reviewId) {
		Review review = null;
		if("TRUE".equalsIgnoreCase(hazelcastCacheSwitch) 
				&& reviewMap().containsKey(reviewId)) {
			review = reviewMap().get(reviewId);
		}
		else {
			Optional<Review> foundReview = repository.findById(reviewId);
			if (foundReview.isPresent()) {
			    review = foundReview.get();
			    reviewMap().put(reviewId, review);
			}
			else {
			    throw new ReviewNotFoundException(reviewNotFound);
			}
		}
		return review;
	}

	@Override
	public List<Review> getReviewByCustomerEmail(String custEmail, Integer pageNumber, Integer count) {
		List<Review> reviewList = null;
		boolean isRequestParamPresent = false;
		if (pageNumber != null && count != null) {
		    isRequestParamPresent = true;
		}
		if (isRequestParamPresent) {
		    reviewList = repository.findByCustomerEmail(custEmail, new PageRequest(pageNumber, count));
	        if (reviewList == null || reviewList.isEmpty()) {
	            throw new ReviewNotFoundException(reviewNotFound);
	        }
	        reviewByCustEmailMap().put(custEmail, reviewList);
		}
		else if ("TRUE".equalsIgnoreCase(hazelcastCacheSwitch) && 
				reviewByCustEmailMap().containsKey(custEmail)) {
			reviewList = reviewByCustEmailMap().get(custEmail);
		}
		else {
			reviewList = repository.findByCustomerEmail(custEmail);
	        if (reviewList == null || reviewList.isEmpty()) {
	            throw new ReviewNotFoundException(reviewNotFound);
	        }
	        reviewByCustEmailMap().put(custEmail, reviewList);
		}
		return reviewList;
	}

	private void updateCache(Review review) {
		reviewMap().put(review.getReviewId(), review);
		if (!reviewByCustEmailMap().containsKey(review.getCustomerEmail())) {
			List<Review> reviewList = new ArrayList<>();
			reviewList.add(review);
			reviewByCustEmailMap().put(String.valueOf(review.getCustomerEmail()), reviewList);
		}
		else {
			List<Review> reviews = reviewByCustEmailMap().get(review.getCustomerEmail());
			reviews.add(review);
			reviewByCustEmailMap().put(String.valueOf(review.getRestaurantId()), reviews);
		}
		if (!reviewByRstIdMap().containsKey(review.getRestaurantId())) {
		    List<Review> reviewList = new ArrayList<>();
            reviewList.add(review);
			reviewByRstIdMap().put(review.getRestaurantId(), reviewList);
		}
		else {
			List<Review> reviews = reviewByRstIdMap().get(review.getRestaurantId());
			reviews.add(review);
			reviewByRstIdMap().put(review.getRestaurantId(), reviews);
		}
	}
	
	private void updateCacheValues(Review review) {
		reviewMap().replace(review.getReviewId(), review);
		List<Review> reviews = reviewByCustEmailMap().get(review.getCustomerEmail());
		for (Review reviewFromCache : reviews) {
			if (reviewFromCache.getReviewId().equals(review.getReviewId())) {
				reviews.remove(reviewFromCache);
				reviews.add(review);
				reviewByCustEmailMap().put(review.getCustomerEmail(), reviews);
				break;
			}
		}
		List<Review> reviewList = reviewByRstIdMap().get(review.getRestaurantId());
		for (Review cacheReview : reviewList) {
            if (cacheReview.getReviewId().equals(review.getReviewId())) {
                reviewList.remove(cacheReview);
                reviewList.add(review);
                reviewByRstIdMap().put(review.getRestaurantId(), reviewList);
            }
        }
	}
	
	private void removeCacheValues(Review review) {
		reviewMap().remove(review.getReviewId());
		
		List<Review> reviewList = reviewByCustEmailMap().get(review.getCustomerEmail());
		Iterator itr = reviewList.iterator(); 
        while (itr.hasNext()) 
        { 
            Review reviewFromCache = (Review) itr.next();
            if (reviewFromCache.getReviewId().equals(review.getReviewId())) {
                itr.remove();
            } 
        } 
        reviewByCustEmailMap().put(review.getCustomerEmail(), reviewList);
		reviewByRstIdMap().remove(review.getRestaurantId());
	}
	
    private boolean isValidRestaurantId(String restaurantId) {
        boolean isValidId = true;
        if ((restaurantId.matches(Pattern.compile("T(-{0,1}(?!0)\\d+)").pattern()))
            || (restaurantId.length() == 1 && restaurantId.equals("0"))) {
            isValidId = false;
        }
        return isValidId;
    }

}
