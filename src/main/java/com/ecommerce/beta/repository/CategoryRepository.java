package com.ecommerce.beta.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.beta.entity.Category;
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>{

}
