package com.back.domain.member.member.controller;


import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {

    @Autowired
    private MockMvc mvc; // MockMvc를 주입받습니다.

    @Autowired
    private MemberService memberService;


    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                                "username": "usernew",
                                                "password": "1234",
                                                "nickname": "유저new"
                                        }
                                        """)
                )
                .andDo(print());

        Member member = memberService.findByUsername("usernew").get();

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getNickname())))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()));
    }


    @Test
    @DisplayName("로그인")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "user1",
                                            "password": "1234"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByUsername("user1").get();

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다.".formatted(member.getNickname())))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.item").exists())
                .andExpect(jsonPath("$.data.item.id").value(member.getId()))
                .andExpect(jsonPath("$.data.item.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.data.item.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.data.item.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.apiKey").value(member.getApiKey()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());


        resultActions.andExpect(
                result -> {
                    Cookie apiKeyCookie= result.getResponse().getCookie("apiKey");
                    assertThat(apiKeyCookie.getValue()).isEqualTo(member.getApiKey()); // 실제 apiKey 값 비교
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.getAttribute("HttpOnly")).isEqualTo("true");

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true");
                }
        );
    }

    @Test
    @DisplayName("내 정보")
    @WithUserDetails("user1")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me")
                )
                .andDo(print());

        Member member = memberService.findByUsername("user1").get();

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%s님 정보입니다.".formatted(member.getNickname())))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()));
    }

        @Test
        @DisplayName("내 정보, with apiKey Cookie")
        @WithUserDetails("user1")
        void t4() throws Exception {

            ResultActions resultActions = mvc
                    .perform(
                            get("/api/v1/members/me")
                    )
                    .andDo(print());

            Member member = memberService.findByUsername("user1").get();

            resultActions
                    .andExpect(handler().handlerType(ApiV1MemberController.class))
                    .andExpect(handler().methodName("me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200-1"))
                    .andExpect(jsonPath("$.msg").value("%s님 정보입니다.".formatted(member.getNickname())))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.id").value(member.getId()))
                    .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.data.nickname").value(member.getNickname()));
    }


    @Test
    @DisplayName("로그아웃")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/members/logout")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))
                .andExpect(
                result -> {
                    Cookie apiKeyCookie= result.getResponse().getCookie("apiKey");
                    assertThat(apiKeyCookie.getValue()).isEmpty();
                    assertThat(apiKeyCookie.getMaxAge()).isEqualTo(0);
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.getAttribute("HttpOnly")).isEqualTo("true");
                }
        );
    }

    @Test
    @DisplayName("엑세스 토큰이 만료되었거나 유효하지 않다면 apiKey를 통해서 재발급")
    void t6() throws Exception {
        Member actor = memberService.findByUsername("user1").get();
        String actorApiKey = actor.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "Bearer " + actorApiKey + " wrong-access-token")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk());

        resultActions.andExpect(
                result -> {
                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true");

                    String headerAuthorization = result.getResponse().getHeader("Authorization");
                    assertThat(headerAuthorization).isNotBlank();

                    assertThat(headerAuthorization).isEqualTo(accessTokenCookie.getValue());
                }
        );
    }

    @Test
    @DisplayName("Authorization 헤더 Bearer 형식이 아닐 때 오류")
    void t7 () throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "key")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-2"))
                .andExpect(jsonPath("$.msg").value("인증 정보가 올바르지 않습니다."));
    }
}
