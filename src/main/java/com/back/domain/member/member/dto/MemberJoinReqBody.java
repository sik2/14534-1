package com.back.domain.member.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberJoinReqBody(
        @NotBlank
        @Size(min = 4, max = 20)
        String username,
        @NotBlank
        @Size(min = 4, max = 30)
        String password,
        @NotBlank
        @Size(min = 2, max = 20)
        String nickname

) {
}
