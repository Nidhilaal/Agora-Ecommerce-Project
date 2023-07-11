package com.ecommerce.beta.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.Product;

@Service
public interface  ProductService {
    Product save(Product product);

    List<Product> findAll();

    Page<Product> findAll(Pageable pageable);

    Product getProduct(UUID uuid);

    void delete(UUID uuid);

    List<Product> findByNameLike(String key);

    Page<Product> findByNameLike(String key, Pageable pageable);

    Page<Product> findByCategory(String filter, Pageable pageable);

    Page<Product> findAllPaged(Pageable pageable);

    Page<Product> findByNameLikePaged(String keyword, Pageable pageable);

    List<Product> findAllEnabled();
}
