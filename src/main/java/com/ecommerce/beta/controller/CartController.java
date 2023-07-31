package com.ecommerce.beta.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.beta.dto.CouponValidityResponseDto;
import com.ecommerce.beta.dto.NewOrderDto;
import com.ecommerce.beta.entity.Cart;
import com.ecommerce.beta.entity.OrderHistory;
import com.ecommerce.beta.entity.OrderItems;
import com.ecommerce.beta.entity.UserInfo;
import com.ecommerce.beta.enums.OrderStatus;
import com.ecommerce.beta.enums.OrderType;
import com.ecommerce.beta.repository.OrderHistoryRepository;
import com.ecommerce.beta.service.AddressService;
import com.ecommerce.beta.service.CartService;
import com.ecommerce.beta.service.OrderHistoryService;
import com.ecommerce.beta.service.OrderItemService;
import com.ecommerce.beta.service.UserInfoService;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    CartService cartService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    OrderHistoryService orderHistoryService;
    @Autowired
    OrderItemService orderItemService;
 
    @Autowired
    AddressService userAddressService;
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;


    @GetMapping("/delete/{uuid}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String delete(@PathVariable UUID uuid){
        cartService.deleteCartById(uuid);
        return "redirect:/viewCart";
    }

    //move items from cart to order
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Transactional
    public String checkout(@ModelAttribute NewOrderDto newOrderDto, Model model){
        //get current user from spring session
        UserInfo userInfo = userInfoService.findByUsername(getCurrentUsername());

        List<Cart> cartItems = cartService.findByUser(userInfo);

        if(!cartItems.isEmpty()){
            System.out.println("Processing COD order from user:"+userInfo.getUsername());
            List<OrderItems> orderItemsList = new ArrayList<>();

            OrderHistory orderHistory = new OrderHistory();
            orderHistory.setOrderStatus(OrderStatus.PROCESSING);
            orderHistory.setOrderType(OrderType.COD);
            orderHistory.setUserInfo(userInfo);
            orderHistory.setUserAddress(userAddressService.findById(newOrderDto.getAddressId()));
            orderHistory = orderHistoryRepository.save(orderHistory);

            for (Cart item : cartItems) {
//
//                if (item.getQuantity() > item.getVariant().getStock()) {
//                    item.setQuantity(item.getVariant().getStock());
//                }

                if (item.getQuantity() != 0) { //if itemQty == 0, after the previous if condition, it means that the product has gone out of stock.
                    OrderItems orderItem = new OrderItems();
//                    orderItem.setVariant(item.getVariant());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setOrderPrice(item.getProductId().getPrice().intValue());
                    orderItem.setOrderHistory(orderHistory);
                    orderItemsList.add(orderItem);
                }
            }


            CouponValidityResponseDto couponValidityResponseDto = cartService.checkCouponValidity();



            float gross = (float) couponValidityResponseDto.getCartTotal();
            float tax = gross / 100f *18f;
            float total = gross - tax;

            //Create Order

            orderHistory.setTotal(total);
            orderHistory.setTax(tax);
            orderHistory.setGross(gross);
            orderHistory.setItems(orderItemsList);

            orderHistory = orderHistoryService.save(orderHistory);

//            for (OrderItems item : orderItemsList) {
//                //reduce stock
//                Variant variant = variantService.findById(item.getVariant().getUuid());
//                System.out.println(variant.getProductId().getName()+" "+variant.getName());
//                variantService.save(variant);
//            }

            //Delete items from cart
            for (Cart item : cartItems) {
                cartService.delete(item);
                System.out.println("Cart Cleared for User:" + userInfo.getUsername());
            }


            return "redirect:/orderDetails?orderId=" + orderHistory.getUuid() + "&newOrderFlag=true";

        }else{
            //fetch order from orderHistory and convert to COD.
            System.out.println("Converting Order to COD");

            OrderHistory order = orderHistoryService.findById(UUID.fromString(newOrderDto.getGeneratedOrderUuid()));

            order.setOrderType(OrderType.COD);

            order.setOrderStatus(OrderStatus.PROCESSING);

            System.out.println("Order Converted to COD");

            return "redirect:/orderDetails?orderId=" + order.getUuid() + "&newOrderFlag=true";

        }
    }

 


    //getting current username
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
