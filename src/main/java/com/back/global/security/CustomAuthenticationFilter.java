package com.back.global.security;

import com.back.global.Rq.Rq;
import com.back.global.exception.ServiceException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("CustomAuthenticationFilter: " + request.getRequestURI());

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


        filterChain.doFilter(request,response);
    }
}
