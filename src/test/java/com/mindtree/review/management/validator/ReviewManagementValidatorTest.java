package com.mindtree.review.management.validator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mindtree.review.management.model.FireBase;
import com.mindtree.review.management.model.Identities;
import com.mindtree.review.management.model.OAuthUser;

public class ReviewManagementValidatorTest {
    
    ReviewManagementValidator validator;

    @Before
    public void setUp() {
        validator = new ReviewManagementValidator();
    }
    @Test
    public void testIsEmailPresent() {
        OAuthUser user = new OAuthUser();
        FireBase firebase = new FireBase();
        Identities identities = new Identities();
        List<String> emailList = new ArrayList<>();
        emailList.add("user@example.com");
        identities.setEmail(emailList);
        firebase.setIdentities(identities);
        user.setFirebase(firebase);
        boolean emailPresent = validator.isEmailPresent(user);
        Assert.assertTrue(emailPresent);
    }

    @Test
    public void testIsEmailPresentWhenFirebaseisNull() {
        OAuthUser user = new OAuthUser();
        boolean emailPresent = validator.isEmailPresent(user);
        Assert.assertFalse(emailPresent);
    }
    
    @Test
    public void testIsEmailPresentWhenIdentitiesisNull() {
        OAuthUser user = new OAuthUser();
        FireBase firebase = new FireBase();
        user.setFirebase(firebase);
        boolean emailPresent = validator.isEmailPresent(user);
        Assert.assertFalse(emailPresent);
    }
    
    @Test
    public void testIsEmailPresentWhenEmailisNull() {
        OAuthUser user = new OAuthUser();
        FireBase firebase = new FireBase();
        Identities identities = new Identities();
        firebase.setIdentities(identities);
        user.setFirebase(firebase);
        boolean emailPresent = validator.isEmailPresent(user);
        Assert.assertFalse(emailPresent);
    
    }
    
    @Test
    public void testIsEmailPresentWhenEmailisEmpty() {
        OAuthUser user = new OAuthUser();
        FireBase firebase = new FireBase();
        Identities identities = new Identities();
        List<String> emailList = new ArrayList<>();
        identities.setEmail(emailList);
        firebase.setIdentities(identities);
        user.setFirebase(firebase);
        boolean emailPresent = validator.isEmailPresent(user);
        Assert.assertFalse(emailPresent);
    }
    
    @Test
    public void testIsEmailPresentWhenUserisNull() {
        OAuthUser user = null;
        boolean emailPresent = validator.isEmailPresent(user);
        Assert.assertFalse(emailPresent);
    }
    
    @Test
    public void testIsValidNumber() {
        boolean numeric = StringUtils.isNumeric("-123");
        Assert.assertFalse(numeric);
    }
}
