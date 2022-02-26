package com.ymastorak.maestros.api.dtos.request;

import com.ymastorak.maestros.persistence.model.MemberType;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Builder
public class MemberRegistrationRequest {
    @NotNull
    private final String name;
    @NotNull
    private final String surname;
    @NotNull
    private final String phone;
    @NotNull
    private final String email;
    @NotNull
    private final MemberType type;

    private final String accessCardId;

    private final String reportsName;

    private final Map<String, Object> memberExtra;

    private final Map<String, Object> eventExtra;
}
