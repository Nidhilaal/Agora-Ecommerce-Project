package com.ecommerce.beta.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.Category;

@Service
public interface CategoryService {

     Category getCategory(UUID uuid);

     List<Category> findAll();

     void addCategory(Category category);

    void delete(UUID uuid);

    void updateCategory(Category category);

    void save(Category category);
}
