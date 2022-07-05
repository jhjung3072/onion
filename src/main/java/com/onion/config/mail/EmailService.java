package com.onion.config.mail;

import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    @Async
    void sendEmail(EmailMessage emailMessage);
}
