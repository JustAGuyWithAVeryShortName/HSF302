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
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "admin-orders";
    }

    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("PAID".equals(order.getStatus())) {
            return "redirect:/admin/orders"; // Already confirmed, do nothing
        }

        order.setStatus("PAID");
        order.setConfirmedAt(LocalDateTime.now());

        // Update User's member plan AND role
        User user = order.getUser();
        if (user != null) {
            user.setMember(order.getMemberPlan());
            // Set the user's role to MEMBER
            user.setRole(Role.Member); // <--- ADD THIS LINE
            userRepository.save(user);


        }

        orderRepository.save(order);

        return "redirect:/admin/orders"; // Redirect to the order management page
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