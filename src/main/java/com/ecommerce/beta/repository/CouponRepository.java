package com.ecommerce.beta.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.beta.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
	
    Coupon findByCode(String coupon);

    Page<Coupon> findByNameLikeOrCodeLike(String s, String t, Pageable pageable);

    List<Coupon> findByApplicableForAndEnabled(UUID uuid, Boolean b);

    List<Coupon> findByCouponTypeAndEnabled(int type, Boolean b);

    List<Coupon> findByApplicableFor(UUID uuid);
}
