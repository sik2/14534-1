package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expireSeconds}")
    private int accessTokenExpireSeconds;

    String genAccessToken(Member member) {
        long id = member.getId();
        String username = member.getUsername();
        String nickname = member.getNickname();

        Map<String, Object> claims = Map.of("id", id, "username", username, "nickname", nickname);

        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpireSeconds,
                claims
        );
    }

    Map<String, Object> payload(String assessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, assessToken);

        if (parsedPayload == null) return null;

        // 값이 Integer든 Long이든 Number로 받아서 longValue() 하면 공통적으로 처리 가능
        long id = ((Number) parsedPayload.get("id")).longValue();

        String username = (String) parsedPayload.get("username");

        String nickname = (String) parsedPayload.get("nickname");

        return Map.of("id", id, "username", username, "nickname", nickname);
    }
}
