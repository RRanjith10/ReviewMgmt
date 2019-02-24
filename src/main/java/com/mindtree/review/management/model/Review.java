package com.mindtree.review.management.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "review_rating")
@ApiModel(value = "Review", description = "Review details")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties(value = { "likeCount", "dislikeCount" }, allowSetters = true)
public class Review implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "review_id", nullable = false)
    @ApiModelProperty(required = true, notes = "Id of the review")
    private Long reviewId;

    @NotNull(message = "Restuarant ID cannot be null")
    @Column(name = "restaurant_id", nullable = false)
    @ApiModelProperty(required = true, notes = "Id of the restaurant")
    private String restaurantId;

    @NotNull(message = "Customer ID cannot be null")
    @Column(name = "customer_email", nullable = false)
    @ApiModelProperty(required = true, notes = "Id of the customer")
    @Email
    private String customerEmail;

    @Column(name = "comments")
    @ApiModelProperty(required = true, notes = "Review comments for the restaurant")
    private String comments;

    @Column(name = "rating", nullable = false)
    @NotNull(message = "Rating cannot be null")
    @ApiModelProperty(required = true, notes = "Rating for the restaurant")
    private Float rating;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reviewedDate", nullable = false)
    @NotNull(message = "Reviewed Date cannot be null")
    @ApiModelProperty(required = true, notes = "Date when review is added for the restaurant")
    private Date reviewedDate;
    
    @ApiModelProperty(notes = "Number of likes for a review comment")
    @Column(name = "like_count", columnDefinition = "bigint(20) default 0")
    private Long likeCount;
    
    @ApiModelProperty(notes = "Number of dislikes for a review comment")
    @Column(name = "dislike_count", columnDefinition = "bigint(20) default 0")
    private Long dislikeCount;

}
