//import java.util.UUID;
//
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import com.ecommerce.beta.entity.OrderHistory;
//import com.ecommerce.beta.enums.OrderStatus;
//import com.ecommerce.beta.service.OrderHistoryService;
//import com.razorpay.Order;
//import com.razorpay.RazorpayClient;
//import com.razorpay.RazorpayException;
//
//@Controller
//@RequestMapping("/payment")
//public class RazorpayController {
//
//    @Value("${razorpay.keyId}")
//    private String razorpayKeyId;
//
//    @Value("${razorpay.keySecret}")
//    private String razorpayKeySecret;
//
//    @Autowired
//    private OrderHistoryService orderHistoryService;
//
//    @GetMapping("/initiate")
//    public String initiatePayment(Model model, @RequestParam("orderUuid") UUID orderUuid) {
//        OrderHistory order = orderHistoryService.findById(orderUuid);
//        model.addAttribute("order", order);
//
//        try {
//            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
//            JSONObject orderRequest = new JSONObject();
//            orderRequest.put("amount", order.getTotal() * 100); // Amount in paise
//            orderRequest.put("currency", "INR");
//            orderRequest.put("receipt", order.getUuid().toString());
//            orderRequest.put("payment_capture", 1); // Auto capture payment
//
//            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
//            model.addAttribute("razorpayOrderId", razorpayOrder.get("id"));
//            model.addAttribute("razorpayKey", razorpayKeyId);
//
//        } catch (RazorpayException e) {
//            // Handle exception
//            e.printStackTrace();
//            return "redirect:/order/" + order.getUuid();
//        }
//
//        return "payment/razorpay";
//    }
//
//    @PostMapping("/callback")
//    public String handlePaymentCallback(@RequestParam("razorpay_order_id") String orderId,
//                                        @RequestParam("razorpay_payment_id") String paymentId,
//                                        @RequestParam("razorpay_signature") String signature) {
//
//        // Verify the signature using your secret key
//        try {
//            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
//            JSONObject attributes = new JSONObject();
//            attributes.put("razorpay_order_id", orderId);
//            attributes.put("razorpay_payment_id", paymentId);
//            attributes.put("razorpay_signature", signature);
//
//            razorpayClient.Utility.verifyPaymentSignature(attributes);
//
//            // Payment is successful, update order status, and handle the success case
//            OrderHistory order = orderHistoryService.findById(UUID.fromString(orderId));
//            order.setOrderStatus(OrderStatus.PAID);
//            orderHistoryService.save(order);
//
//            // Redirect to a success page or show a success message
//            return "redirect:/order/" + order.getUuid() + "?payment_success=true";
//        } catch (RazorpayException | SignatureVerificationException e) {
//            // Handle exception, log error, and show failure message
//            e.printStackTrace();
//            return "redirect:/order/" + orderId + "?payment_failed=true";
//        }
//    }
//}
