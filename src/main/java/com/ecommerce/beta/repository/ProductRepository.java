package com.ecommerce.beta.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.beta.entity.Category;
import com.ecommerce.beta.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID>{
	
	 List<Product> findByNameLike(String key);
	 
	 Page<Product> findByNameLike(String key, Pageable pageable);
	 
	 Page<Product> findByNameContainingAndEnabledIsTrue(String key, Pageable pageable);

	 Page<Product> findAllByEnabledTrue(Pageable pageable);

	 List<Product> findAllByEnabledTrue();

	 Page<Product> findByCategory(Category category, Pageable pageable);

}
