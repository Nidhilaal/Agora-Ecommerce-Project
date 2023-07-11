package com.ecommerce.beta.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.beta.entity.Otp;
import com.ecommerce.beta.entity.UserInfo;
@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
	
	Otp findByUserInfo(UserInfo user);
}
