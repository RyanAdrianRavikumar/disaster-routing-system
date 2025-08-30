package com.tursa.notification.sms;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}
