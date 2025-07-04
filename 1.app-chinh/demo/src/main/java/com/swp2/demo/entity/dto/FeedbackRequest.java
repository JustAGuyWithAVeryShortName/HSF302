package com.swp2.demo.entity.dto; // Ensure this package matches your project structure

public class FeedbackRequest {
    private Integer rating; // Using Integer to allow for null if no rating is provided
    private String comment;

    // Getters
    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    // Setters (Lombok's @Data would provide these automatically)
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Optional: toString method for easier debugging
    @Override
    public String toString() {
        return "FeedbackRequest{" +
            "rating=" + rating +
            ", comment='" + comment + '\'' +
            '}';
    }
}