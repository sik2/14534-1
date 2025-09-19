package com.back.domain.member.member.dto;

public record MemberLoginResBody(
        MemberDto item,
        String apiKey,
        String accessToken
) {
}
