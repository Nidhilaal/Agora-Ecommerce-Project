package com.ecommerce.beta.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.OnlineOrderRef;
import com.ecommerce.beta.repository.OnlineOrderRefRepository;
import com.ecommerce.beta.service.OnlineOrderRefService;

@Service
public class OnlineOrderRefServiceImpl implements OnlineOrderRefService {
    @Autowired
    OnlineOrderRefRepository onlineOrderRefRepository;

    @Override
    public OnlineOrderRef save(OnlineOrderRef onlineOrderRef) {
        return onlineOrderRefRepository.save(onlineOrderRef);
    }

    @Override
    public OnlineOrderRef findByRazorpayOrderId(String orderId) {
        return onlineOrderRefRepository.findByRazorPayOrderId(orderId);
    }
}
