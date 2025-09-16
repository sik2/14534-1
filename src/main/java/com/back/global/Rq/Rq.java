package com.back.global.Rq;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.service.PostService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Rq {
    private final PostService postService;
    private final HttpServletRequest req;
    private final MemberService memberService;

    public Member getActor() {
        String headerAuthorization =  req.getHeader("Authorization");

        if (headerAuthorization == null || headerAuthorization.isBlank()) {
            throw new ServiceException("401-1", "Authorization 헤더가 존재 하지 않습니다.");
        }

        if (!headerAuthorization.startsWith("Bearer ")) {
            throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");
        }

        String apiKey = headerAuthorization.substring("Bearer ".length()).trim();

        Member member = memberService
                .findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));

        return member;
    }
}
