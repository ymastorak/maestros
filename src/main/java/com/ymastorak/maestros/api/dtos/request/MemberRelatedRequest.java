package com.ymastorak.maestros.api.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRelatedRequest {
    @Min(1)
    @NotNull
    private Integer memberId;

    private Map<String, Object> eventExtra;
}
