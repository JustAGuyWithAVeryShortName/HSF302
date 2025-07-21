package com.swp2.demo.entity.dto;


public class PaymentCallbackEvent {
    private Long paymentOrderCode;
    private boolean success;
    private String message;

    public PaymentCallbackEvent() {
    }
    public PaymentCallbackEvent(Long paymentOrderCode, boolean success, String message) {
        this.paymentOrderCode = paymentOrderCode;
        this.success = success;
        this.message = message;
    }
    public Long getPaymentOrderCode() {
        return paymentOrderCode;
    }
    public void setPaymentOrderCode(Long paymentOrderCode) {
        this.paymentOrderCode = paymentOrderCode;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
