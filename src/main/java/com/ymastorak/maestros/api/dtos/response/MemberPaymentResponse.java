package com.ymastorak.maestros.api.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MemberPaymentResponse extends MemberRelatedResponse {
    @Min(0)
    private BigDecimal amountUsed;
    @Min(0)
    private BigDecimal amountLeft;
}
