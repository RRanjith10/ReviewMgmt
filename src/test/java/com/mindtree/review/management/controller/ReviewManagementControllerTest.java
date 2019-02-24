package com.mindtree.review.management.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import com.mindtree.review.management.ReviewManagementServiceApplication;
import com.mindtree.review.management.exception.ReviewNotFoundException;
import com.mindtree.review.management.model.Review;
import com.mindtree.review.management.service.impl.ReviewManagementServiceImpl;


@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = ReviewManagementServiceApplication.class)
public class ReviewManagementControllerTest {

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    @Autowired
    MappingJackson2HttpMessageConverter jacksonConverter;
    @MockBean
    public ReviewManagementServiceImpl service;
    
    @TestConfiguration
    public class ModifyReviewManagementServiceTestContextConfiguration {
        @Bean
        public ReviewManagementServiceImpl globalSummitService() {
            return service;
        }
    }

    @Test
    public void testAddReview() {
        Mockito.when(service.addNewReview(Mockito.any(Review.class), Mockito.anyString())).thenReturn(new Review());
        MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        Review review = buildNewReview("Comments", 1L, "4.5", "user@example.com", "100");
        request.body(convertToJson(review));
        request.header("Content-Type", "application/json");
        request.when().post("/review").then().statusCode(201);
    }
    
    @Test
    public void testUpdateReview() {
        Review review = buildNewReview("Comments", 1L, "4.5", "user@example.com", "100");
        Mockito.when(service.getReviewById(1L)).thenReturn(review);
        Mockito.when(service.updateReview(Mockito.any(Review.class), Mockito.anyString())).thenReturn(review);
        MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        request.body(convertToJson(review));
        request.header("Content-Type", "application/json");
        request.when().put("/review").then().statusCode(200);
    }
    
    @Test
    public void testDeleteReview() {
        Mockito.when(service.removeReview(Mockito.any(String.class), Mockito.anyString())).thenReturn(true);
        MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        request.when().delete("/review/{restaurant_id}", 100).then().statusCode(204);
    }
    
    @Test
    public void testDeleteReviewForInvalidReviewId() {
        Mockito.when(service.removeReview(Mockito.any(String.class), Mockito.anyString())).thenThrow(ReviewNotFoundException.class);
        MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        request.when().delete("/review/{restaurant_id}", 100).then().statusCode(404);
    }
    @Test
    public void testGetAllReviewsWhenNoReviewsArePresent() {
        Mockito.when(service.getReviewByCustomerEmail(Mockito.anyString(), Mockito.anyInt(),
            Mockito.anyInt())).thenReturn(null);
        MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        request.when().get("/review").then().statusCode(200);
    }
    @Test
    public void testGetReviewsForRestaurant() {
        Review review = buildNewReview("Comments", 1L, "4.5", "user@example.com", "100");
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review);
        Mockito.when(service.getReviewByRestaurantId(Mockito.anyString(), Mockito.anyInt(),
            Mockito.anyInt())).thenReturn(reviewList);
        MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        request.when().get("/review/{restaurant_id}", 100).then().statusCode(200);
    }
    @Test
    public void testUpdateLikeCount() {
    	Mockito.when(service.updateLikeCount(Mockito.any(Long.class))).thenReturn(1);
    	MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        request.when().put("/review/{review_id}/like", 1).then().statusCode(200);
    }
    
    @Test
    public void testUpdateDisLikeCount() {
    	Mockito.when(service.updateDisLikeCount(Mockito.any(Long.class))).thenReturn(1);
    	MockMvcRequestSpecification request = RestAssuredMockMvc.given().mockMvc(mvc);
        request.when().put("/review/{review_id}/dislike", 1).then().statusCode(200);
    }
    private String convertToJson(Review request) {

        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        try {
            jacksonConverter.write(request, MediaType.APPLICATION_JSON, outputMessage);
            System.out.println(outputMessage.getBody().toString());
            return outputMessage.getBody().toString();
        }
        catch (HttpMessageNotWritableException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private Review buildNewReview(String comments, long reviewId, String rating, String customerEmail, String restaurantId) {
        Review review = new Review();
        review.setComments(comments);
        review.setCustomerEmail(customerEmail);
        review.setRating(Float.parseFloat(rating));
        review.setRestaurantId(restaurantId);
        review.setReviewId(reviewId);
        review.setReviewedDate(new Date());
        return review;
    }

}
