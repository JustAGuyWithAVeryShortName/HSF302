package com.swp2.demo.Controller;


import com.swp2.demo.entity.Order;
import com.swp2.demo.entity.Role;
import com.swp2.demo.entity.User;

import com.swp2.demo.repository.OrderRepository;
import com.swp2.demo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.dto.PaymentRequestDTO;
import com.swp2.demo.entity.dto.PaymentResponeDTO;
import com.swp2.demo.service.MemberService;
import com.swp2.demo.service.PayOSService;
import com.swp2.demo.service.UserService;

import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${webhook.base-uri}")
    private String webhookBaseUri;
    private final PayOSService payOSService;
    private final MemberService memberService;
    private final UserService userService;

    public PaymentController(PayOSService payOSService, MemberService memberService, UserService userService) {
        this.payOSService = payOSService;
        this.memberService = memberService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentResponeDTO> createPayment(HttpSession session, @RequestBody PaymentRequestDTO request) {

        PaymentResponeDTO responsePayment;
        User user = (User) session.getAttribute("loggedInUser");

        try {
            responsePayment = payOSService.createPaymentLink(request, user);
            return ResponseEntity.ok(responsePayment);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/info/{orderCode}")
    public ResponseEntity<?> getPaymentInfo(@PathVariable long orderCode) {
        return ResponseEntity.ok(payOSService.getPaymentInfo(orderCode));
    }
    @GetMapping("/cancel")
    public ResponseEntity<?> handleCancelRedirect(@RequestParam long orderCode) {
        log.info("❌ Người dùng đã hủy thanh toán cho đơn hàng {}", orderCode);
        return ResponseEntity.ok(payOSService.cancelPayment(orderCode, "User cancelled the payment"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Webhook webhook) {
        try {
            log.info("📩 Received webhook data: {}", webhook.getData());
            log.info("📩 Webhook signature: {}", webhook.getSignature());

            WebhookData data = payOSService.verifyWebhook(webhook);
            if (data == null) {
                log.error("❌ Invalid webhook data received");
                return ResponseEntity.badRequest().body("Invalid webhook data");
            }

            log.info("🔄 Processing payment status: {} for order: {}", data.getCode(), data.getOrderCode());

            switch (data.getCode()) {
                case "00" -> {
                    log.info("✅ Payment successful | OrderCode: {} | Amount: {} | Description: {}",
                            data.getOrderCode(), data.getAmount(), data.getDesc());

                    // Process membership upgrade for successful payments
                    processMembershipUpgrade(data);
                }
                case "01" -> {
                    log.info("❌ Payment canceled | OrderCode: {} | Description: {}",
                            data.getOrderCode(), data.getDesc());
                }
                case "02" -> {
                    log.info("⏳ Payment pending | OrderCode: {} | Description: {}",
                            data.getOrderCode(), data.getDesc());
                }
                default -> {
                    log.warn("⚠️ Unknown status code: {} | OrderCode: {}",
                            data.getCode(), data.getOrderCode());
                }
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("❌ Webhook processing failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Webhook processing failed");
        }
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private void processMembershipUpgrade(WebhookData data) {
        try {
            // Extract membership type from payment description
            String description = data.getDesc();
            Member targetMembership = extractMembershipFromDescription(description);
            Long orderCode = data.getOrderCode();

            if (targetMembership == null) {
                log.warn("❗ Không tìm thấy loại thành viên phù hợp từ mô tả: {}", description);
                return;
            }

            Order order = orderRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));

            User user = order.getUser();
            if (user == null) {
                log.error("❌ Đơn hàng không có người dùng gắn với orderCode: {}", orderCode);
                return;
            }

            order.setStatus("PAID");
            order.setConfirmedAt(LocalDateTime.now());
            orderRepository.save(order);

            user.setMember(targetMembership);
            user.setRole(Role.Member); // đảm bảo đúng tên enum/chuỗi bạn dùng
            userRepository.save(user);

            log.info("✅ Đã nâng cấp user {} lên gói {} với orderCode {}", user.getEmail(), targetMembership, orderCode);
        } catch (Exception e) {
            log.error("❌ Failed to process membership upgrade for order {}: {}",
                data.getOrderCode(), e.getMessage());
        }
    }

    private Member extractMembershipFromDescription(String description) {
        if (description == null) return null;

        String upperDesc = description.toUpperCase();
        if (upperDesc.contains("VIP")) {
            return Member.VIP;
        } else if (upperDesc.contains("PREMIUM")) {
            return Member.PREMIUM;
        }
        return null;
    }

    @PostMapping("/confirm-webhook")
    public ResponseEntity<String> confirmWebhook() {
        // Remove /payment since it's already in context-path
        String webhookUrl = webhookBaseUri + "/api/payments/webhook";
        try {
            payOSService.confirmWebhook(webhookUrl);
            log.info("✅ Webhook registered with URL: {}", webhookUrl);
            return ResponseEntity.ok("Webhook registered successfully.");
        } catch (Exception e) {
            log.error("❌ Failed to register webhook: {}", e.getMessage(), e);
            return new ResponseEntity<>("Webhook registration failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
