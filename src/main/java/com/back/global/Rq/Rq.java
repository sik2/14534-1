package com.back.global.Rq;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.service.PostService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final PostService postService;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final MemberService memberService;

    public Member getActor() {
        String headerAuthorization = getHeader("Authorization", "");

        String apiKey;
        String accessToken;

        // headerAuthorization이 존재한다면
        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "인증 정보가 올바르지 않습니다.");
            }

            // ["Bearer", apiKey, accessToken]
            String[] headerAuthorizations =  headerAuthorization.split(" ", 3);

            apiKey = headerAuthorizations[1];
            accessToken = headerAuthorizations.length == 3 ? headerAuthorizations[2] : "";
        } else { // headerAuthorization 존재하지 않는다면 쿠키에서 정보가지고 오기
            apiKey = getCookieValue("apiKey", "");
            accessToken = getCookieValue("accessToken", "");
        }

        /*
            검증
           1. accessToken, apiKey(refreshToken)가지고 있는지 검증
        */

        if (apiKey.isBlank()) throw new ServiceException("401-1", "로그인 후 사용해주세요.");

        Member member = null;

        if (!accessToken.isBlank()) {
            Map<String, Object> payload = memberService.payload(accessToken);

            // TODO: 보완 예정
            if (payload != null) {
                String username = (String) payload.get("username");
                // 좋은 코드가 아니다 -> DB 조회를 한다
                member = memberService.findByUsername(username)
                        .orElseThrow(() ->new ServiceException("401-3", "회원을 찾을 수 없습니다."));
            }
        }
        // DB 조회를 이용한 회원검증 - RefreshKey 역할
        member = memberService
                .findByApiKey(apiKey)
                .orElseThrow((() ->new ServiceException("401-3", "회원을 찾을 수 없습니다.")));

        return member;
    }

    private String getHeader(String name, String defaultValue) {
        return Optional
                        .ofNullable(req.getHeader("Authorization"))
                        .filter(headerValue -> !headerValue.isBlank())
                        .orElse(defaultValue);
    }

    private String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(req.getCookies())
                                        .filter(cookie -> name.equals(cookie.getName()))
                                        .map(Cookie::getValue)
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 쿠키를 도메인 전체에서 쓰겠다.
        cookie.setHttpOnly(true); // 쿠키를 스크립트로 접근 못하게(XSS 공격방어)

        if (value.isBlank()) {
            cookie.setMaxAge(0);
        }

        resp.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}
