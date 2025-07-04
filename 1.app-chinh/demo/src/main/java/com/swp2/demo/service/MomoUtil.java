package com.swp2.demo.service;

import com.swp2.demo.entity.MomoRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.UUID;

public class MomoUtil {
    public static MomoRequest createRequest(String partnerCode, String accessKey, String secretKey,
                                            String endpoint, String returnUrl, String notifyUrl,
                                            Long amount, String orderInfo,
                                            String partnerName, String storeId) {

        String requestId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        String requestType = "captureWallet";
        String lang = "vi";
        String extraData = "";

        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSHA256(rawHash, secretKey);

        return new MomoRequest(
                partnerCode,
                partnerName,
                storeId,
                requestId,
                amount.toString(),
                orderId,
                orderInfo,
                returnUrl,
                notifyUrl,
                lang,
                requestType,
                signature,
                extraData
        );
    }
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static String hmacSHA256(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes("UTF-8"));
            return bytesToHex(hash);   // trả về dạng hex chứ không phải base64
        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA256 signing error", e);
        }
    }
    public static boolean verifySignature(Map<String, String> params, String secretKey) {
        String rawHash = "accessKey=" + params.get("accessKey") +
                "&amount=" + params.get("amount") +
                "&extraData=" + params.get("extraData") +
                "&message=" + params.get("message") +
                "&orderId=" + params.get("orderId") +
                "&orderInfo=" + params.get("orderInfo") +
                "&orderType=" + params.get("orderType") +
                "&partnerCode=" + params.get("partnerCode") +
                "&payType=" + params.get("payType") +
                "&requestId=" + params.get("requestId") +
                "&responseTime=" + params.get("responseTime") +
                "&resultCode=" + params.get("resultCode") +
                "&transId=" + params.get("transId");

        String expectedSignature = MomoUtil.hmacSHA256(rawHash, secretKey);
        return expectedSignature.equals(params.get("signature"));
    }

}
