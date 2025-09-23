package com.back.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("favicon.ico").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
                                // 게시글 다건 단건, 댓글 다건 단건 요청 권한을 전체 허용하겠다.
                                // \\d+ -> 숫자가 한 자리 이상 연속된 것 (ex. 1, 23, 123)
                                .requestMatchers(HttpMethod.GET, "/api/*/posts/{id:\\d+}",
                                        "/api/*/posts", "/api/*/posts/{postId:\\d+}/comments",
                                        "/api/*/posts/{postId:\\d+}/comments/{id:\\d+}").permitAll()
                                .requestMatchers("/api/*/members/login", "/api/*/members/logout").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/*/members").permitAll()
                                .requestMatchers("/api/*/adm/**").hasRole("ADMIN") // 관리자 권한 체크(선언적으로 인가 처리)
                                .requestMatchers("/api/*/**").authenticated()
                                .anyRequest().permitAll()
                )
                .headers(
                        headers -> headers
                                .frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                ).csrf(
                        AbstractHttpConfigurer::disable
                )
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(401);
                                            response.getWriter().write(
                                                    """
                                                            {
                                                                 "resultCode": "401-1",
                                                                 "msg": "로그인 후 사용해주세요."
                                                            }
                                                            """
                                            );
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(403);
                                            response.getWriter().write(
                                                    """
                                                            {
                                                                 "resultCode": "403-1",
                                                                 "msg": "권한이 없습니다."
                                                            }
                                                            """
                                            );
                                        }
                                )
                );
        return http.build();
    }
}
