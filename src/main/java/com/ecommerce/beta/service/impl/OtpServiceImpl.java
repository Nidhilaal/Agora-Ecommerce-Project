package com.ecommerce.beta.service.impl;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.Otp;
import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.repository.OtpRepository;
import com.ecommerce.beta.service.OtpService;
import com.ecommerce.beta.service.UserInfoService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

@Service
public class OtpServiceImpl implements OtpService {
	
	@Autowired
	OtpRepository otpRepository;
	
	@Autowired
	UserInfoService userInfoService;

	@Override
	public void save(Otp otp) {
		otpRepository.save(otp);
	}

	@Override
	public Otp findByUser(UserInfo user) {
		return otpRepository.findByUserInfo(user);
	}

	@Override
	public void delete(Otp oldOtp) {
		otpRepository.delete(oldOtp);
		
	}

	@Override
	public boolean verifyPhoneOtp(String otp, String phone) {
		UserInfo user =userInfoService.findByPhone(phone);
		if(user.isVerified()) {
			return true;
		}
		Otp savedOtp=this.findByUser(user);
		if(savedOtp.isotpRequired()) {
			if(otp.equals(savedOtp.getOtp())) {
				user.setVerified(true);
				userInfoService.save(user);
				this.delete(savedOtp);
				return true;
			}
		}
		return false;
	}

    public void sendPhoneOtp(String phoneNumber){

        UserInfo user = userInfoService.findByPhone(phoneNumber);
        Otp savedOtp= this.findByUser(user);

        if(savedOtp != null){
            if (savedOtp.isotpRequired()) {
                System.out.println("valid otp already exists");
                return;
            }else{
                System.out.println("Deleting expired otp");
                otpRepository.delete(savedOtp);
            }
        }

            String otp = generateOTP();
            Otp generatedOtp = new Otp(otp);
            generatedOtp.setUserInfo(user);
            generatedOtp = otpRepository.save(generatedOtp);
            System.out.println("new otp generated");
   
         final String ACCOUNT_SID = "Your Account_SID";
         final String AUTH_TOKEN = "Your Auth_Token";

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        PhoneNumber from = new PhoneNumber("Your Twilio phone number");
        PhoneNumber to = new PhoneNumber("+91 + Your 10 digit phone Number which you have registered with twilio");


        String content = "This is your account verification OTP. Valid for 5 minutes. OTP: " + generatedOtp.getOtp();
        MessageCreator messageCreator = new MessageCreator(to, from,  content);
        Message res = messageCreator.create();
        System.out.println(res.getSid());
    }
	
    private String generateOTP() {
        // Generate a random 6-digit OTP
        Random random = new Random();
        int otpNumber = 100_000 + random.nextInt(900_000);
        return String.valueOf(otpNumber);
    }
	
}
