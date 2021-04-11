package com.hackillionis.talktalk.data;

public class FeedbackData {

    String rating, feedback, timestamp, listener, speaker;

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        this.listener = listener;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public FeedbackData(String rating, String feedback, String timestamp, String listener, String speaker) {
        this.rating = rating;
        this.feedback = feedback;
        this.timestamp = timestamp;
        this.listener = listener;
        this.speaker = speaker;
    }
}
