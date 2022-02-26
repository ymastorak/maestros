package com.ymastorak.maestros.api.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ymastorak.maestros.persistence.model.Member;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadReportResponse extends MaestrosServiceResponse {
    private List<Member> members;
    private BigDecimal legacyOutstanding;
    private BigDecimal currentOutstanding;
    private BigDecimal totalOutstanding;
}
