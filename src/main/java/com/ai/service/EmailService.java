package com.ai.service;

public interface EmailService {

    boolean sendVerificationEmail(String to, String subject, String code, String type);

    String getEmailCode(String email, String type);

    void removeEmailCode(String email, String type);

    boolean sendEmailCode(String email, String type);

    Long getCodeExpire(String email, String type);

    void checkImageCode(String email, String imageCode);
}
