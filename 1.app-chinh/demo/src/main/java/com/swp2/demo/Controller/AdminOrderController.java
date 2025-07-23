package com.swp2.demo.Controller;

import com.swp2.demo.repository.OrderRepository;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.entity.Order;
import com.swp2.demo.entity.Role; // Import the Role enum
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listOrders(Model model) {
        // Sử dụng phương thức mới để lấy tất cả orders
        List<Order> orders = orderRepository.findAllOrdersWithUser();
        model.addAttribute("orders", orders);
        return "admin-orders";
    }

    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if ("UNPAID".equals(order.getStatus())) {
            order.setStatus("PAID");
            order.setConfirmedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return "redirect:/admin/orders";
    }

    @PostMapping("/{orderId}/delete")
    public String deleteOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User user = order.getUser();

        // If order was paid, reset User's member plan and role
        if ("PAID".equals(order.getStatus()) && user != null) {
            user.setMember(null); // or user.setMember(Member.FREE); based on your logic
            user.setRole(Role.Guest); // <--- Consider resetting role to Guest or a default un-paid role
            userRepository.save(user);
        }

        orderRepository.delete(order);
        return "redirect:/admin/orders";
    }
}