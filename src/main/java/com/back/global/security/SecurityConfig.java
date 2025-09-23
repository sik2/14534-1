package com.back.global.security;

import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                )
                .csrf(AbstractHttpConfigurer::disable) // csrf 보호기능 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성
                .logout(AbstractHttpConfigurer::disable) // 로그아웃 기능 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .sessionManagement(AbstractHttpConfigurer::disable) // 세션 관리 비활성화
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(401);
                                            response.getWriter().write(
                                                    Ut.json.toString(
                                                            new RsData<Void>(
                                                                    "401-1",
                                                                    "로그인 후 이용해주세요."
                                                            )
                                                    )
                                            );
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(403);
                                            response.getWriter().write(
                                                    Ut.json.toString(
                                                            new RsData<Void>(
                                                                    "403-1",
                                                                    "권한이 없습니다."
                                                            )
                                                    )
                                            );
                                        }
                                )
                );
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정
        configuration.setAllowedOrigins(List.of("https://cdpn.io", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));

        // 자격 증명 허용 설정
        configuration.setAllowCredentials(true);

        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));

        // CORS 설정을 소스에 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
