package com.tursa.notification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsoleSmsService implements SmsService {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleSmsService.class);

    @Override
    public void sendSms(String phoneNumber, String message) {
        // Simulated SMS sending (logs)
        logger.info("FAKE SMS -> to {} : {}", phoneNumber, message.length() > 200 ? message.substring(0,200) + "..." : message);
    }
}
