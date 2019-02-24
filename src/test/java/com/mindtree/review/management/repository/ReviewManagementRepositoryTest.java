package com.mindtree.review.management.repository;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mindtree.review.management.model.Review;

@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
public class ReviewManagementRepositoryTest {
    @Autowired
    private ReviewManagementRepository repository;

    @Test
    public void testFindbyRestIdAndEmail() {
        Review findByRestaurantIdInAndCustomerEmail = repository.findByRestaurantIdInAndCustomerEmail("100", "user@example.com");
        Assert.assertNotNull(findByRestaurantIdInAndCustomerEmail);
        Assert.assertEquals("Nice", findByRestaurantIdInAndCustomerEmail.getComments());
        Assert.assertEquals(4F, findByRestaurantIdInAndCustomerEmail.getRating().longValue(), 2);
    }
    
    @Test
    public void testFindByRestaurantId() {
        List<Review> findByRestaurantId = repository.findByRestaurantId("100");
        Assert.assertNotNull(findByRestaurantId);
        Assert.assertEquals(2, findByRestaurantId.size());
    }
    @Test
    public void testFindByCustomerEmail() {
        List<Review> findByCustomerEmail = repository.findByCustomerEmail("user@example.com", new PageRequest(0, 5));
        Assert.assertNotNull(findByCustomerEmail);
        Assert.assertEquals(2, findByCustomerEmail.size());
    }
    @Test
    public void testUpdateLikeCount() {
    	int updateLikeCount = repository.updateLikeCount(1L);
    	Assert.assertEquals(1, updateLikeCount);
    }
    @Test
    public void testUpdateLikeCountForInvalidId() {
    	int updateLikeCount = repository.updateLikeCount(10L);
    	Assert.assertEquals(0, updateLikeCount);
    }
    @Test
    public void testUpdateDisLikeCount() {
    	int updateLikeCount = repository.updateDislikeCount(1L);
    	Assert.assertEquals(1, updateLikeCount);
    }
    @Test
    public void testUpdateDisLikeCountForInvalidId() {
    	int updateLikeCount = repository.updateDislikeCount(10L);
    	Assert.assertEquals(0, updateLikeCount);
    }
}
