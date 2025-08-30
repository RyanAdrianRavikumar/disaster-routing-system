package com.tursa.notification.dto;

public class NotificationRequest {
    private String userRfid;
    private String phoneNumber;
    private String shelterName;
    private String newRoute;
    private String message;

    public String getUserRfid() { return userRfid; }
    public void setUserRfid(String userRfid) { this.userRfid = userRfid; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }

    public String getNewRoute() { return newRoute; }
    public void setNewRoute(String newRoute) { this.newRoute = newRoute; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
