package com.ymastorak.maestros.api.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberBlacklistRequest extends MemberRelatedRequest {
    @NotEmpty
    private String blacklistReason;
}
