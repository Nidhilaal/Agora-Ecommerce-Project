package com.ecommerce.beta.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class cartDto {
    UUID uuid;
    int qty;
    double cartTotal = 0;
    double cartSaved = 0;
}
//DTO to get updated cart details