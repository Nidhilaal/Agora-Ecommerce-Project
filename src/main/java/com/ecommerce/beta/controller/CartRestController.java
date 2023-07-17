package com.ecommerce.beta.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.beta.dto.cartDto;
import com.ecommerce.beta.entity.Cart;
import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.entity.Variant;
import com.ecommerce.beta.service.CartService;
import com.ecommerce.beta.service.UserInfoService;
import com.ecommerce.beta.service.VariantService;

@RestController
@RequestMapping("/cart")
public class CartRestController {
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    CartService cartService;
    @Autowired
    VariantService variantService;

    @PostMapping("/add")
    public String addToCart(@RequestBody Map<String, String> request){
        String currentUsername = String.valueOf(getCurrentUsername());
        if(currentUsername.equals("anonymousUser")){
            return "login";
        }



        String variantId = request.get("variantId");
        int qty = Integer.parseInt(request.get("qty"));


        if(qty<0  || qty>100){
            return "invalid quantity, accepted values are  0-100";
        }

        if(cartService.addToCart(variantId,qty)){
            return "added";
        }else{
            return "not added";
        }
    }
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String deleteFromCart(@RequestParam Variant variantId){
        //get current user
        String currentUsername = String.valueOf(getCurrentUsername());
        //UserInfo userInfo = userInfoService.findByUsername(currentUsername);
        UserInfo userInfo = userInfoService.findByUsername("test");

        List<Cart> carts = cartService.findByUser(userInfo);
        String deletedStatus = "not deleted";
        for(Cart cart : carts){
            if(cart.getVariant().equals(variantId)){
                cartService.delete(cart);
                deletedStatus = "deleted";
                break;
            }
        }
        return deletedStatus;
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String updateCart(@RequestBody List<cartDto> updatedCart){
        List<Cart> existingCart = cartService.findByUser(userInfoService.findByUsername(getCurrentUsername()));

       for(cartDto item : updatedCart){
           if(item.getQty() == 0){
               cartService.deleteCartById(item.getUuid());
               System.out.println(item.getUuid()+ " deleted");

           }else{
               Cart existingCartItem = cartService.findCartByUuid(item.getUuid());
               if(existingCartItem.getQuantity() != item.getQty()){
                   System.out.println(item.getUuid()+ " updated");
                   existingCartItem.setQuantity(item.getQty());
                   cartService.save(existingCartItem);
               }
           }
       }

        return "redirect:/viewCart";
    }


    //getting current username
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
