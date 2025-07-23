package com.swp2.demo.service;

import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.Order;
import com.swp2.demo.entity.User;
import com.swp2.demo.repository.OrderRepository;
import com.swp2.demo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.payos.PayOS;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.swp2.demo.config.PayOSConfig;
import com.swp2.demo.entity.dto.PaymentRequestDTO;
import com.swp2.demo.entity.dto.PaymentResponeDTO;


@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSService {
    private final String REDIRECT_URI = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();
    private final PayOSConfig payOSConfig;
    @Autowired
    private final PayOS payOS;

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    public PaymentResponeDTO createPaymentLink(PaymentRequestDTO request,User user ) {
        try {
            long orderCode = System.currentTimeMillis();


            Order order = new Order();
            order.setUser(user);
            order.setMemberPlan(Member.VIP);
            order.setOrderCode(orderCode);
            order.setAmount(request.getAmount().doubleValue());
            order.setStatus("CREATED"); // hoặc "UNPAID"
            orderRepository.save(order);

            String description = request.getDescription();
            if (description != null && description.length() > 25) {
                description = description.substring(0, 25);
            }

            PaymentData data = PaymentData.builder()
                    .amount(request.getAmount())
                    .description(description)
                    .orderCode(orderCode)
                    .cancelUrl(REDIRECT_URI + "/member/cancel?orderCode=" + orderCode)
                    .returnUrl(REDIRECT_URI + "/member/success?orderCode=" + orderCode)
                    .build();

            System.out.println("📤 Gửi thanh toán: orderCode = " + orderCode + ", amount = " + request.getAmount());

            String url = payOS.createPaymentLink(data).getCheckoutUrl();

            return PaymentResponeDTO.builder()
                    .checkoutUrl(url)
                    .orderCode(orderCode)
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gọi PayOS: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo link thanh toán PayOS", e);
        }
    }
    public PaymentLinkData getPaymentInfo(long orderCode) {
        try {
            System.out.println("🔍 Đang truy vấn đơn hàng orderCode = " + orderCode);
            return payOS.getPaymentLinkInformation(orderCode);
        } catch (Exception e) {
            System.err.println("❌ Không tìm thấy đơn hàng: " + orderCode);
            throw new RuntimeException("Lỗi khi lấy thông tin đơn hàng", e);
        }
    }
    public PaymentLinkData cancelPayment(long orderCode, String reason) {
        try {
            // Kiểm tra và cập nhật đơn hàng trong database
            Order order = orderRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            log.info("eee");
            order.setStatus("CANCELLED");
            log.info("zzzz");
            orderRepository.save(order);

            // Hủy thanh toán trên PayOS
            return payOS.cancelPaymentLink(orderCode, reason);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi hủy đơn hàng", e);
        }
    }
    public WebhookData verifyWebhook(Webhook data) {
        try {
            WebhookData webhookData = data.getData(); // ✅ Lấy trực tiếp

            System.out.println("📩 Webhook nhận được:");
            System.out.println("👉 Mã đơn: " + webhookData.getOrderCode());
            System.out.println("👉 Trạng thái: " + webhookData.getCode());
            System.out.println("👉 Mô tả: " + webhookData.getDesc());

            return webhookData;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý webhook test", e);
        }
    }
    public void confirmWebhook(String webhookUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(payOSConfig.getApiKey()); // lấy từ cấu hình

        Map<String, String> body = Map.of("webhookUrl", webhookUrl);
        HttpEntity<?> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.payos.vn/v1/webhook/confirm", request, String.class);

            log.info("✅ Webhook confirm response: {} - {}", response.getStatusCode(), response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("⚠️ Webhook registration failed. Reason: {}", response.getBody());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("❌ HTTP Error: Status {} - Body {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("❌ Unexpected error while confirming webhook", e);
        }
    }

    private User getCurrentUser(Object principal) {
        if (principal instanceof CustomUserDetails userDetails) {
            return userService.findById(userDetails.getId());
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return userService.findByEmail(email);
        }
        return null;
    }
}
