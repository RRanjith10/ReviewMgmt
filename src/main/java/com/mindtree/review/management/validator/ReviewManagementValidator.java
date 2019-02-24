package com.mindtree.review.management.validator;

import com.mindtree.review.management.model.OAuthUser;

public class ReviewManagementValidator {

    public boolean isEmailPresent(OAuthUser user) {
        return user != null && user.getFirebase() != null && user.getFirebase().getIdentities() != null
            && user.getFirebase().getIdentities().getEmail() != null
            && !user.getFirebase().getIdentities().getEmail().isEmpty();
    }
    
}
