package com.tursa.notification.dto;

import java.util.List;

public class BroadcastRequest {
    private List<String> userRfids;
    private String message;

    public List<String> getUserRfids() { return userRfids; }
    public void setUserRfids(List<String> userRfids) { this.userRfids = userRfids; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
