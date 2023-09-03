package com.ecommerce.beta.service;

import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.OnlineOrderRef;

@Service
public interface OnlineOrderRefService {
	
    OnlineOrderRef save(OnlineOrderRef onlineOrderRef);

    OnlineOrderRef findByRazorpayOrderId(String orderId);
}
