package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.Rq.Rq;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final Rq rq;
    private final MemberService memberService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("CustomAuthenticationFilter: " + request.getRequestURI());


        try {
            work(request, response, filterChain);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json");
            response.setStatus(rsData.statusCode());
            response.getWriter().write(
                    Ut.json.toString(rsData)
            );
        } catch(Exception e) {
            throw e;
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // API 요청 아니라면 패스
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증, 인가가 필요없는 API 요청 이라면 패스
        if (List.of("/api/v1/members/login", "/api/v1/members/logout",
                "/api/v1/members/join").contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey;
        String accessToken;

        String headerAuthorization = rq.getHeader("Authorization", "");

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
            apiKey = rq.getCookieValue("apiKey", "");
            accessToken = rq.getCookieValue("accessToken", "");
        }

        logger.debug("apiKey: " + apiKey);
        logger.debug("accessToken: " + accessToken);

        if (apiKey.isBlank() && accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Member member = null;
        boolean isAccessTokenExists = !accessToken.isBlank();
        boolean isAccessTokenValid = false;

        if (isAccessTokenExists) {
            Map<String, Object> payload = memberService.payload(accessToken);

            if (payload != null) {
                long id = ((Number) payload.get("id")).longValue();
                String username = (String) payload.get("username");
                String nickname = (String) payload.get("nickname");
                member = new Member(id, username, nickname);

                // 토큰 유효성 검증 성공
                isAccessTokenValid = true;
            }
        }

        if (member == null) {
            member = memberService.findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-3", "회원을 찾을 수 없습니다."));
        }

        // 토큰 존재하고, 토큰 유효성 검증 실패 했을 때
        if (isAccessTokenExists && !isAccessTokenValid) {
            // apiKey(refresh token)을 이용한 accessToken 재발급
            String actorAccessToken = memberService.genAccessToken(member);

            rq.setCookie("accessToken", actorAccessToken);
            // 비교용으로 전달
            rq.setHeader("Authorization", actorAccessToken);
        }

        UserDetails user = new SecurityUser(
                member.getId(),
                member.getUsername(),
                "",
                member.getNickname(),
                member.getAuthorities()
        );


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                "",
                user.getAuthorities()
        );

        // 이 시점 이후부터는 시큐리티가 이 요청을 인증된 사용자의 요청으로 취급
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request,response);
    }
}
