package com.ecommerce.beta.service;

import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.Otp;
import com.ecommerce.beta.entity.UserInfo;
@Service
public interface OtpService {
	
	void save(Otp otp);

    Otp findByUser(UserInfo user);

    void delete(Otp oldOtp);

    boolean verifyPhoneOtp(String otp, String phone);

    void sendPhoneOtp(String phoneNumber);

}
