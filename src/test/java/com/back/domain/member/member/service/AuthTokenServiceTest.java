package com.back.domain.member.member.service;

import com.back.standard.util.Ut;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthTokenServiceTest {
    @Autowired
    private AuthTokenService authTokenService;

    @Test
    @DisplayName("authTokenService가 존재한다.")
    void t1() {
        assertThat(authTokenService).isNotNull();
    }

    @Test
    @DisplayName("jjwt 최신 방식으로 jwt 생성, {name= \"Paul\", age=23}")
    void t2 () {
        // 서명키 (SecretKey)
        String jwtSecretKey = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";
        byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

        // 클레임 - 사용자 정보
        Map<String, Object> claims = Map.of("name", "Paul", "age", "23");

        // 생성시간, 만료시간 설정
        long expireMillis = 1000L * 60 * 60 * 24 * 365; // 토큰 만료시간을 1년
        Date issuedAt = new Date(); // 발행시간 설정
        Date expiration = new Date(issuedAt.getTime() + expireMillis); //발행 시간으로 부터 만료시간 설정

        String jwt = Jwts.builder()
                .claims(claims) // 사용자 정보
                .issuedAt(issuedAt) // 생성날짜
                .expiration(expiration) // 만료날짜
                .signWith(secretKey) // 키 서명
                .compact();

        assertThat(jwt).isNotBlank();

        System.out.println("jwt : " + jwt);
    }

    @Test
    @DisplayName("Ut.jwt.toString 통해서 jwt 생성, {name= \"Paul\", age=23}")
    void t3 () {
        String secret = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";
        int expireSeconds = 1000 * 60 * 60 * 24 * 365; // 토큰 만료시간을 1년
        Map<String, Object> claims = Map.of("name", "David", "age", "20");

        String jwt = Ut.jwt.toString(
                secret,
                expireSeconds,
                claims
        );

        assertThat(jwt).isNotBlank();

        System.out.println("jwt : " + jwt);
    }
}
