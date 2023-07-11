package com.ecommerce.beta.entity;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Otp {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "org.hibernate.type.UUIDCharType")
	private UUID uuid;
	
	private Date otpRequestTime;
	
	private String otp;
	
	@OneToOne
	@JoinColumn(name = "user_id")
	UserInfo userInfo;
	
	private static final long OTP_VALID_DURATION =5*60*1000;
	public boolean isotpRequired() {
		if(this.getOtp()==null) {
			return false;
		}
		long currentTimeInMillis=System.currentTimeMillis();
		long otpRequestedTimeinMillis=this.otpRequestTime.getTime();
		return otpRequestedTimeinMillis + OTP_VALID_DURATION >= currentTimeInMillis;
	
	} 
	public Otp(String otp) {
		this.otp=otp;
		this.otpRequestTime=new Date();
	}

}
