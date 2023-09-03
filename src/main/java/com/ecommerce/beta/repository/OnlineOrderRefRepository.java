package com.ecommerce.beta.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.beta.entity.OnlineOrderRef;

@Repository
public interface OnlineOrderRefRepository extends JpaRepository<OnlineOrderRef, UUID> {
	
    OnlineOrderRef findByRazorPayOrderId(String orderId);
    //razorPayOrderId
}
