package com.ecommerce.beta.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.beta.dto.UserDto;
import com.ecommerce.beta.entity.Address;
import com.ecommerce.beta.entity.Category;
import com.ecommerce.beta.entity.Product;
import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.service.AddressService;
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
	
	@Autowired
	AddressService addressService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
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
	
	//display user profile page
    @GetMapping("/profile")
    public String viewProfile(@RequestParam(name="addAddress", defaultValue = "false", required = false)boolean addAddress,
                              Model model) {

        String currentUsername = String.valueOf(getCurrentUsername());
        if (currentUsername.equals("anonymousUser")) {
            return "redirect:/login";
        }
        if (currentUsername.equals("anonymousUser")) {
            model.addAttribute("loggedIn", false);
        } else {
            model.addAttribute("loggedIn", true);
        }

        UserInfo userInfo = userInfoService.findByUsername(getCurrentUsername());


        boolean noAddressFound = false;

        if (userInfo.getSavedAddresses().size() == 0 || Objects.equals(userInfo.getSavedAddresses().get(0).getFlat(), null) || Objects.equals(userInfo.getSavedAddresses().get(0).getFlat(), "")) {
            model.addAttribute("setupAddressWarning", true);
            noAddressFound = true;
            //create an empty address if it's null
//            if(userInfo.getSavedAddresses().get(0) == null){
//
//                UserAddress userAddress = new UserAddress();
//                userAddress.setUserInfo(userInfo);
//                userAddress = userAddressService.save(userAddress);
//                List<UserAddress> list = new ArrayList<>();
//                list.add(userAddress);
//                userInfo.setSavedAddresses(list);
//                userInfo = userInfoService.save(userInfo);
//            }
        }

//        model.addAttribute("wishlistCount", userInfoService.findByUsername(currentUsername).getWishlist().size());

        model.addAttribute("user", userInfo);


        List<Address> addresses;
        if (noAddressFound) {
            addresses = new ArrayList<>();
//            addresses.add(new UserAddress());
            System.out.println("No addresses found");
        } else {
            addresses = addressService.findByUser(userInfo);

        }
        model.addAttribute("addresses", addresses);

//        Map<String, Object> userPageValues = currentUserPageValues();


        //show add address error
        model.addAttribute("addAddress",addAddress);


        return "shop/profile";
    }
    
    @PostMapping("/address/save")
    public String save(@ModelAttribute Address userAddress) {
        UserInfo user = userInfoService.findByUsername(getCurrentUsername());
        userAddress.setUserInfo(user);

        List<Address> userAddressList = addressService.findByUser(user);

        if (userAddressList.isEmpty()) {
            // Saving first address
            userAddress.setDefaultAddress(true);
            addressService.save(userAddress);
            return "redirect:/profile";
        }else {
            if(userAddress.isDefaultAddress()){
                Address existingDefaultAddress = userAddressList.stream()
                        .filter(Address::isDefaultAddress)
                        .findFirst()
                        .orElse(null);

                if (existingDefaultAddress != null) {
                    existingDefaultAddress.setDefaultAddress(false);
                    //remove default from previous default address
                    addressService.save(existingDefaultAddress);
                }
                //save new address(default)
                addressService.save(userAddress);
            }
        }

        return "redirect:/profile";
    }
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/user/save")
    public String save(@ModelAttribute UserDto userDto,
                       BindingResult bindingResult){
            UserInfo user = userInfoService.findByUsername(getCurrentUsername());
            if(userDto.getUuid().equals(user.getUuid())){
                user.setFirstName(userDto.getFirstName());
                user.setLastName(userDto.getLastName());
                user.setPhone(userDto.getPhone());
                user.setEmail(userDto.getEmail());

                //save new and updated addresses


               //check if there is password change and change password if required

                if(passwordEncoder.matches(userDto.getPassword(), user.getPassword())){
                    System.out.println("matched");
                    if(!userDto.getNewPassword().equals("") &&  userDto.getNewPassword().equals(userDto.getNewPasswordRe())){
                        user.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
                    }

                }else{
                    System.out.println("not matched");
                }

                userInfoService.updateUser(user);
            }else{
                return "404";
            }

        return "redirect:/profile";
    }
		
}
