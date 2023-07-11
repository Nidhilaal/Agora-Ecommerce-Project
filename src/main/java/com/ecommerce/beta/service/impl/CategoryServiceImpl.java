package com.ecommerce.beta.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.beta.entity.Category;
import com.ecommerce.beta.repository.CategoryRepository;
import com.ecommerce.beta.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryRepository categoryRepository;
    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void delete(UUID uuid) {
        categoryRepository.deleteById(uuid);
    }

    @Override
    public void updateCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void save(Category category) {
        categoryRepository.save(category);
    }

    public List<Category> findAll() {
       return categoryRepository.findAll();
    }

    public Category getCategory(UUID uuid) {
        return categoryRepository.findById(uuid).orElse(null);
    }
}
