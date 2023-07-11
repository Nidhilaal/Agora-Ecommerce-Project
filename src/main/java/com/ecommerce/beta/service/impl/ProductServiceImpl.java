package com.ecommerce.beta.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.Image;
import com.ecommerce.beta.entity.Product;
import com.ecommerce.beta.repository.ProductRepository;
import com.ecommerce.beta.service.CategoryService;
import com.ecommerce.beta.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
	
    @Autowired
    ImageServiceImpl imageService;
    @Autowired
    ProductRepository productRepository;
    
    @Autowired
    CategoryService categoryService;

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAllByEnabledTrue(pageable);
    }

    @Override
    public Product getProduct(UUID uuid) {
        return productRepository.findById(uuid).orElse(null);
    }

    //cannot delete a product because it has dependencies;
    @Override
    public void delete(UUID uuid) {
        List<Image> images = imageService.findImagesByProductId(uuid);
        if(!images.isEmpty()){
            for (Image image : images) {
                imageService.delete(image.getUuid());
            }
        }
        productRepository.deleteById(uuid);
    }

    @Override
    public List<Product> findByNameLike(String key) {
        return productRepository.findByNameLike(key);
    }

    @Override
    public Page<Product> findByNameLike(String key, Pageable pageable) {
        return productRepository.findByNameContainingAndEnabledIsTrue(key, pageable);
    }

    @Override
    public Page<Product> findByCategory(String filter, Pageable pageable) {
        try {
            UUID uuid = UUID.fromString(filter); // Check if the string is a valid UUID
            return productRepository.findByCategory(categoryService.getCategory(uuid), pageable);
        } catch (IllegalArgumentException e) {
   
            return Page.empty();
        }
    }

    @Override
    public Page<Product> findAllPaged(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findByNameLikePaged(String keyword, Pageable pageable) {
        return productRepository.findByNameLike("%"+keyword+"%", pageable);
    }

    @Override
    public List<Product> findAllEnabled() {
        return productRepository.findAllByEnabledTrue();
    }
}
