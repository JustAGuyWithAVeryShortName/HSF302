package com.swp2.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;

@Component

public class WebhookConfig implements CommandLineRunner {
    @Autowired
    private PayOS payOS;
    @Value("${webhook.base-uri}")
    private String webhookBaseUri;

    @Override
    public void run(String... args) throws Exception {
        String webHookUrl = webhookBaseUri + "/api/payments/webhook";
        try {
            String result = payOS.confirmWebhook(webHookUrl);
            System.out.println("✅ Webhook confirmed: " + result);
        } catch (Exception e) {
            System.err.println("❌ Failed to register webhook: " + e.getMessage());
        }
    }

}
