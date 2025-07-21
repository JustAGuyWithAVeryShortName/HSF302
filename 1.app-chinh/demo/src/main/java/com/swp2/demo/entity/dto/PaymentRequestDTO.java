package com.swp2.demo.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDTO {
    private Integer amount;
    private String description;

    public PaymentRequestDTO() {
    }
    public PaymentRequestDTO(Integer amount, String description) {
        this.amount = amount;
        this.description = description;
    }
}
