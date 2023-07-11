package com.ecommerce.beta.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.service.OtpService;
import com.ecommerce.beta.service.ProductService;
import com.ecommerce.beta.service.UserInfoService;

@Controller
@EnableAsync
public class LoginController {
	
	@Autowired
	JavaMailSender javaMailSender;
	
	@Autowired
	UserInfoService userInfoService;
	
	@Autowired
	OtpService otpService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	ProductService productService;
	
	public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
	
	@GetMapping("/login")
    public String showLoginForm(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {

            model.addAttribute("signedUp", false);

            return "login-page";

        }
          return "redirect:/"; 
    }
	
	@GetMapping("/resetPassword")
    public String resetPassword(@RequestParam(value = "error", required = false, defaultValue = "false") Boolean error, Model model){
        model.addAttribute("error", error);

        return "resetPassword";
    }
	
	@PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("email") String email){
        UserInfo user = userInfoService.findByEmail(email);
        //delete existing otps
        if (user == null) {
            System.out.println("No user found with email "+email);
            return "redirect:/resetPassword?error=true";
        }
        else {
//            Otp oldOtp = otpService.findByUser(user);
//            if (oldOtp != null)
//                otpService.delete(oldOtp);

            // Generate password
            String newPassword =generatePassword();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setVerified(true);
            userInfoService.updateUser(user);
            sendNewPassword(user.getEmail(),newPassword);

            return "redirect:/checkEmailForPassword";
        }
        
	}
	
	@GetMapping("/enterOtp")
    public String enterOtp(@RequestParam("email") String email, Model model) {

        model.addAttribute("email",email);

        return "enterOtp";
    }
	
    @GetMapping("/checkEmailForPassword")
    public String checkEmail(){
        return "checkEmailForPassword";
    }
    
    @Async
    private void sendEmail(String email, String otp) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("msnidhilal16@gmail.com"); // Set your "from" address here
        mailMessage.setTo(email);
        mailMessage.setSubject("Reset Password OTP");
        mailMessage.setText("Your OTP for passwrdrreset: " + otp);

        javaMailSender.send(mailMessage);
    }
	
    private void sendNewPassword(String email, String password) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("msnidhilal16@gmail.com"); // Set your "from" address here
        mailMessage.setTo(email);
        mailMessage.setSubject("Password Reset");
        mailMessage.setText("Your new password is: " + password);

        javaMailSender.send(mailMessage);
    }

    @PostMapping("/verify")
    public String verifyUser(@RequestParam(value = "otp") String otp,
                             @RequestParam(value = "phone") String phone,
                             Model model){

       boolean verified =  otpService.verifyPhoneOtp(otp, phone);
       if(verified){
           return "redirect:/";
       }else {
    	   model.addAttribute("failed", true);
           return "verification";
    	   
       }      
    }
    private String generatePassword() {
        int passwordLength = 8; 
        String allowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder passwordBuilder = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = random.nextInt(allowedCharacters.length());
            char randomChar = allowedCharacters.charAt(randomIndex);
            passwordBuilder.append(randomChar);
        }

        return passwordBuilder.toString();
    }
    
	private String generateOTP() {
        // Generate a random 6-digit OTP
        Random random = new Random();
        int otpNumber = 100_000 + random.nextInt(900_000);
        return String.valueOf(otpNumber);
    }
}
