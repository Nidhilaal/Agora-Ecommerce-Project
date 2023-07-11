package com.ecommerce.beta.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.beta.entity.Category;
import com.ecommerce.beta.entity.Product;
import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.service.CategoryService;
import com.ecommerce.beta.service.OtpService;
import com.ecommerce.beta.service.ProductService;
import com.ecommerce.beta.service.UserInfoService;

@Controller
public class ShopController {
	
	@Autowired
	UserInfoService userInfoService;
	
	@Autowired
	OtpService otpService;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	CategoryService categoryService;
	
	public String getCurrentUsername() {
		Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
	
	@GetMapping("/")
	public String getHomePage(@RequestParam(required = false, defaultValue = "") String keyword,
			@RequestParam(required = false,defaultValue = "") String filter,			
			Model model) {
		
	    String currentUsername = String.valueOf(getCurrentUsername());
        UserInfo userInfo = userInfoService.findByUsername(currentUsername);
        
        List<Product> products;
        List<Category> categories=categoryService.findAll();
        
		if (!isVerified(currentUsername)) {
            String phone = userInfo.getPhone();
            model.addAttribute("phone", phone);
        
            //send otp
            otpService.sendPhoneOtp(phone);
        
            return "verification"; 
            
		}else {
			if(!keyword.equals("")&& filter.equals("")){
				products=productService.findByNameLike("%" + keyword + "%")
						.stream()
					    .filter(product -> !product.getCategory().isDeleted())
					    .filter(product -> !product.isEnabled())
			            .filter(product -> filter.equals(""))
					    .collect(Collectors.toList());
						
			}else if (!filter.equals("")&& keyword.equals("")) {
				products = productService.findAll()
					    .stream()
					    .filter(product -> product.getCategory().getUuid().toString().equals(filter))

					    .filter(product -> !product.getCategory().isDeleted())
					    .filter(product -> !product.isEnabled())
					    .collect(Collectors.toList());
								
			}else if (!keyword.equals("")&& !filter.equals("")) {
				products = productService.findByNameLike("%" + keyword + "%")
					    .stream()
					    .filter(product -> !product.getCategory().isDeleted())
					    .filter(product -> !product.isEnabled())
					    .filter(product -> product.getCategory().getUuid().toString().equals(filter))
					    .collect(Collectors.toList());
				
			}else {
				products = productService.findAll()
					    .stream()
					    .filter(product -> !product.getCategory().isDeleted())
					    .filter(product -> !product.isEnabled())
					    .collect(Collectors.toList());
				
			}
			model.addAttribute("categories",categories);
			model.addAttribute("products", products);

			return "shop/index";
		}
	}
	
	private boolean isVerified(String username) {
		UserInfo user = userInfoService.findByUsername(username);
		if (user.getUuid() != null) {
			return user.isVerified();
		} else {
			return true;
		}
	}
	
	@GetMapping("/productDetail")
	public String productView(@RequestParam(value = "productUuid", required = false) String productUuid,
			Model model){
		
		Product selectedProduct=new Product();
		selectedProduct= productService.getProduct(UUID.fromString(productUuid));
        model.addAttribute("product", selectedProduct);
        return "shop/productView";
	}
	
	@GetMapping("/addToCart")
	public String cartView(@RequestParam(value = "productUuid", required = false) String productUuid,
			Model model) {
		Product addedProduct= new Product();
		addedProduct=productService.getProduct(UUID.fromString(productUuid));
		model.addAttribute(addedProduct);
		return "shop/checkout";
	}
		
}
