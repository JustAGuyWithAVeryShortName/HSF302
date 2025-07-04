package com.swp2.demo.Controller;


import com.swp2.demo.repository.OrderRepository;
import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.Order;
import com.swp2.demo.entity.User;
import com.swp2.demo.security.CustomUserDetails;
import com.swp2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

@Controller
public class PaymentController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/payment/status")
    @ResponseBody
    public ResponseEntity<String> checkPaymentStatus(@RequestParam("transactionId") Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> ResponseEntity.ok(order.getStatus())) // Có đơn ➔ trả về 200 + status
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found")); // Không có đơn ➔ 404 + message
    }


    @GetMapping("/payment")
    public String payment(@RequestParam("plan") String plan,
                          @AuthenticationPrincipal Object principal,
                          Model model) {
        User user = null;

        if (principal instanceof CustomUserDetails userDetails) {
            user = userService.findById(userDetails.getId());
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            user = userService.findByEmail(email);
        }

        if (user == null) {
            return "redirect:/login";
        }


        double amount = switch (plan) {
            case "VIP" -> 5000.0;
            case "PREMIUM" -> 10000.0;
            case "FREE" -> 0.0;
            default -> throw new IllegalArgumentException("Invalid plan: " + plan);
        };

        Member memberPlan = Member.valueOf(plan.toUpperCase());
        Order order = orderRepository.findFirstByUserAndMemberPlanAndStatus(user, Member.valueOf(plan), "PENDING")
                .orElse(null);

        if (order == null) {
            order = new Order();
            order.setUser(user);
            order.setMemberPlan(memberPlan);
            order.setAmount(amount);
            order.setStatus(amount == 0.0 ? "PAID" : "PENDING");
            order.setCreatedAt(LocalDateTime.now());
            if (amount == 0.0) {
                order.setConfirmedAt(LocalDateTime.now());
                user.setMember(Member.FREE);
                userService.save(user);
            }
            orderRepository.save(order);
        }

        model.addAttribute("plan", plan);
        model.addAttribute("amount", (int) amount);
        model.addAttribute("transactionId", order.getId());
        model.addAttribute("status", order.getStatus());
        return "payment";
    }
    @GetMapping("/success")
    public String success() {
        return "success";
    }

}















