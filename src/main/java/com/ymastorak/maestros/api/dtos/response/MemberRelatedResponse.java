package com.ymastorak.maestros.api.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ymastorak.maestros.persistence.model.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberRelatedResponse extends MaestrosServiceResponse {
    private Member member;
}
