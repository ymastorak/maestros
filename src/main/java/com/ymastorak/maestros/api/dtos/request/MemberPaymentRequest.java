package com.ymastorak.maestros.api.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPaymentRequest extends MemberRelatedRequest {
    @NotNull
    @Min(0)
    private BigDecimal amount;
}
