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

@Component
@RequiredArgsConstructor
public class Rq {
    private final PostService postService;
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final MemberService memberService;

    public Member getActor() {
        String headerAuthorization =  req.getHeader("Authorization");

        if (headerAuthorization == null || headerAuthorization.isBlank()) {
            throw new ServiceException("401-1", "로그인 후 사용해주세요.");
        }

        if (!headerAuthorization.startsWith("Bearer ")) {
            throw new ServiceException("401-2", "인증 정보가 올바르지 않습니다.");
        }

        String apiKey = headerAuthorization.substring("Bearer ".length()).trim();

        Member member = memberService
                .findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "회원을 찾을 수 없습니다."));

        return member;
    }

    public void setCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 쿠키를 도메인 전체에서 쓰겠다.
        cookie.setHttpOnly(true); // 쿠키를 스크립트로 접근 못하게(XSS 공격방어)

        resp.addCookie(cookie);
    }
}
