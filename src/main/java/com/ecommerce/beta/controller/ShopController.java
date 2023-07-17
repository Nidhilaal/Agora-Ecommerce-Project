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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.beta.dto.UserDto;
import com.ecommerce.beta.entity.Address;
import com.ecommerce.beta.entity.Cart;
import com.ecommerce.beta.entity.Category;
import com.ecommerce.beta.entity.Product;
import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.entity.Variant;
import com.ecommerce.beta.service.AddressService;
import com.ecommerce.beta.service.CartService;
import com.ecommerce.beta.service.CategoryService;
import com.ecommerce.beta.service.ImageService;
import com.ecommerce.beta.service.OtpService;
import com.ecommerce.beta.service.ProductService;
import com.ecommerce.beta.service.UserInfoService;

@Controller
public class ShopController {
	
	@Autowired
    UserInfoService userInfoService;
    @Autowired
    AddressService userAddressService; 
    @Autowired
    OtpService otpService;
    @Autowired
    PasswordEncoder passwordEncoder;    
    @Autowired
    CategoryService categoryService;
    @Autowired
    ImageService imageService;
    @Autowired
    ProductService productService;
    @Autowired
    CartService cartService;
	
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
	                          @RequestParam(value = "variantUuid", required = false) String variantUuid,
	                          Model model) {
	    Product selectedProduct = productService.getProduct(UUID.fromString(productUuid));
	    List<Variant> selectedVariants = selectedProduct.getVariants();

	    Variant selectedVariant = null;
	    if (variantUuid != null) {
	        for (Variant variant : selectedVariants) {
	            if (variant.getUuid().equals(variantUuid)) {
	                selectedVariant = variant;
	                break;
	            }
	        }
	    }

	    model.addAttribute("product", selectedProduct);
	    model.addAttribute("selectedVariant", selectedVariant);
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
            addresses = userAddressService.findByUser(userInfo);

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

        List<Address> userAddressList = userAddressService.findByUser(user);

        if (userAddressList.isEmpty()) {
            // Saving first address
            userAddress.setDefaultAddress(true);
            userAddressService.save(userAddress);
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
                    userAddressService.save(existingDefaultAddress);
                }
                //save new address(default)
                userAddressService.save(userAddress);
            }
        }

        return "redirect:/profile";
    }
    
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
    
    @GetMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable("id") UUID uuid ) {
    	userAddressService.deleteById(uuid);
    	return "redirect:/profile";
    }
    
    @GetMapping("viewCart")
    public String viewCart(Model model,
                           @RequestParam(required = false) UUID addressUUID
                           ) {
        String currentUsername = String.valueOf(getCurrentUsername());
        if (currentUsername.equals("anonymousUser")) {
            return "redirect:/login";
        }

        UserInfo userInfo = userInfoService.findByUsername(getCurrentUsername());



        long count = userInfo.getSavedAddresses()
                .stream()
                .filter(a -> a.isEnabled())
                .count();

        if (count == 0) {
            System.out.println("No addresses found for user, redirecting to profile");
           return "redirect:/profile?addAddress=true";
        }


        List<Cart> cartItems = cartService.findByUser(userInfo);

        List<Address> addressList = userAddressService.findByUserInfoAndEnabled(userInfo,true);

        model.addAttribute("addressList", addressList);

        model.addAttribute("nameOfUser", userInfo.getFirstName() + " " + userInfo.getLastName());


        if (cartItems.size() == 0) {
            model.addAttribute("cartEmpty", true);
        } else {
            model.addAttribute("cartEmpty", false);
        }

     


        model.addAttribute("loggedIn", true);



        model.addAttribute("cartItems", cartItems);
        //find cart total
        double cartTotal = cartItems.stream()
                .mapToDouble(cartItem -> cartItem.getVariant().getSellingPrice() * cartItem.getQuantity())
                .sum();
        //mrp
        double cartMrp = cartItems.stream()
                .mapToDouble(cartItem -> cartItem.getVariant().getPrice() * cartItem.getQuantity())
                .sum();

        float total = (float) cartTotal;
        float tax = total / 100 * 18; //18%
        total -= tax;
        float gross = total + tax;


        model.addAttribute("cartTotal", Math.round(total));

        model.addAttribute("cartTax", Math.round(tax));

        model.addAttribute("cartSaved", Math.round(cartMrp - cartTotal));

        model.addAttribute("cartMrp", Math.round(cartMrp));

        List<Address> addresses = userAddressService.findByUserInfoAndEnabled(userInfo, true);

        Address defaultAddress = addresses
                .stream()
                .filter(Address::isDefaultAddress) // Filter addresses with isDefaultAddress true
                .findFirst() // Find the first matching address
                .orElse(null);
        if(defaultAddress == null){
            defaultAddress = addresses.get(0);
        }


        if (addressUUID == null) {
            model.addAttribute("address", defaultAddress);

        } else {
            model.addAttribute("address", userAddressService.findById(addressUUID));
        }

        //coupon redemption logic
        /*if (couponCode != null && !expired) {
            System.out.println("Coupon: " + couponCode);
            Coupon coupon = couponService.findByCode(couponCode);
            if (coupon == null) {
                System.out.println("Invalid coupon");
            } else {

                if (coupon.isExpired()) {
                    return "redirect:/viewCart?expired=true&couponCode=" + couponCode;
                }

                CouponValidityResponseDto couponValidityResponseDto = cartService.checkCouponValidity();

                if(couponValidityResponseDto.isValid()){
                    System.out.println("Coupon is valid");

                     total = (float) couponValidityResponseDto.getCartTotal();
                     tax = total / 100 * 18; //18%
                     total -= tax;
                     gross = total + tax;

                    model.addAttribute("cartTax", Math.round(tax));
                    model.addAttribute("priceOff", Math.round(couponValidityResponseDto.getPriceOff()));
                    model.addAttribute("cartTotal", Math.round(total));
                    model.addAttribute("cartSaved", Math.round(cartMrp - cartTotal + couponValidityResponseDto.getPriceOff()));

                        model.addAttribute("couponApplied", true);
                        model.addAttribute("couponCode", couponCode);
                }else{
                        model.addAttribute("couponApplied", false);
                        model.addAttribute("couponCode", "");
                        System.out.println("Coupon is invalid");

                    model.addAttribute("priceOff", Math.round(couponValidityResponseDto.getPriceOff()));
                    model.addAttribute("cartTotal", Math.round(couponValidityResponseDto.getCartTotal()));
                    model.addAttribute("cartSaved", Math.round(cartMrp - cartTotal - couponValidityResponseDto.getPriceOff()));
                }
*/

                //check coupon type
//                switch (coupon.getCouponType()) {
//                    case 1 -> {
//                        System.out.println("Product Coupon");
//                        boolean productFoundInCart = false;
//                        Float productPrice = 0F;
//                        Product couponProduct = productService.getProduct(coupon.getApplicableFor());
//                        if (couponProduct == null) {
//                            System.out.println("Product in coupon not found");
//                        } else {
//                            for (Cart item : cartItems) {
//                                if (item.getVariant().getProductId().equals(couponProduct)) {
//                                    System.out.println("Product in coupon found in cart");
//                                    productFoundInCart = true;
//                                    productPrice = item.getVariant().getSellingPrice();
//                                    break;
//                                }
//                            }
//                            if (productFoundInCart) {
//                                float priceOff = productPrice / 100 * coupon.getOffPercentage();
//
//                                priceOff = priceOff > coupon.getMaxOff() ? coupon.getMaxOff() : priceOff;
//
//                                model.addAttribute("couponApplied", true);
//                                model.addAttribute("couponCode", couponCode);
//                                model.addAttribute("priceOff", Math.round(priceOff));
//                                model.addAttribute("cartTotal", Math.round(cartTotal - priceOff));
//                                model.addAttribute("cartSaved", Math.round(cartMrp - cartTotal + priceOff));
//
//                            }
//                        }
//                    }
//                    case 2 -> {
//                        System.out.println("Category Coupon");
//                        boolean categoryFound = false;
//                        float offerPrice = 0F;
//                        Category category = categoryService.getCategory(coupon.getApplicableFor());
//                        if (category == null) {
//                            System.out.println("Category Not Found");
//                        } else {
//                            for (Cart item : cartItems) {
//                                if (item.getVariant().getProductId().getCategory().equals(category)) {
//                                    System.out.println("Category in coupon found in cart item");
//                                    categoryFound = true;
//                                    offerPrice += item.getVariant().getSellingPrice();
//                                }
//                            }
//                            if (categoryFound) {
//                                float priceOff = offerPrice / 100 * coupon.getOffPercentage();
//                                priceOff = priceOff > coupon.getMaxOff() ? coupon.getMaxOff() : priceOff;
//
//                                model.addAttribute("couponApplied", true);
//                                model.addAttribute("couponCode", couponCode);
//                                model.addAttribute("priceOff", Math.round(priceOff));
//                                model.addAttribute("cartTotal", Math.round(cartTotal - priceOff));
//                                model.addAttribute("cartSaved", Math.round(cartMrp - cartTotal + priceOff));
//
//                            } else {
//                                System.out.println("No items in cart belong to the coupon category");
//                            }
//                        }
//                    }
//                    case 3 -> System.out.println("Brand Coupon");
//                    case 4 -> {
//                        System.out.println("General Coupon");
//                        float priceOff = (float) (cartTotal / 100F * coupon.getOffPercentage());
//                        priceOff = priceOff > coupon.getMaxOff() ? coupon.getMaxOff() : priceOff;
//                        model.addAttribute("couponApplied", true);
//                        model.addAttribute("couponCode", couponCode);
//                        model.addAttribute("priceOff", Math.round(priceOff));
//                        model.addAttribute("cartTotal", Math.round(cartTotal - priceOff));
//                        model.addAttribute("cartSaved", Math.round(cartMrp - cartTotal + priceOff));
//                    }
//                    case 5 -> System.out.println("User Coupon");
//                    default -> System.out.println("Unknown Coupon Type");
//                }
//            }


        


        return "shop/cart";
    }
    
    
 
}
  
