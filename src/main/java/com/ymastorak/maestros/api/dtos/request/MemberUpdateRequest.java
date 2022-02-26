package com.ymastorak.maestros.api.dtos.request;

import com.ymastorak.maestros.persistence.model.MemberType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest extends MemberRelatedRequest {
    @NotNull
    private String name;
    @NotNull
    private String surname;
    @NotNull
    private String phone;
    @NotNull
    private String email;
    @NotNull
    private MemberType type;

    private String reportsName;

    private Map<String, Object> memberExtra;
}
