package com.ymastorak.maestros.api.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaestrosServiceResponse {
    public enum ServiceOutcome {SUCCESS, FAILED}

    @Builder.Default
    private ServiceOutcome outcome = ServiceOutcome.SUCCESS;
    private String error;
    private String errorDetails;
}
