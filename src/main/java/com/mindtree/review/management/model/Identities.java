package com.mindtree.review.management.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Identities implements Serializable {

    private List<String> name;
    private List<String> email;
}
