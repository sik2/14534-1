package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1AdmMemberControllerTest {

    @Autowired
    private MockMvc mvc; // MockMvc를 주입받습니다.

    @Autowired
    private MemberService memberService;


    @Test
    @DisplayName("회원 다건조회")
    @WithUserDetails("admin")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/members")
                )
                .andDo(print());

        List<Member> members = memberService.findAll();

        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1AdmMemberController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(members.size()));

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(member.getId()))
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].nickname".formatted(i)).value(member.getNickname()))
                    .andExpect(jsonPath("$[%d].username".formatted(i)).value(member.getUsername()));
        }
    }

    @Test
    @DisplayName("회원 단건조회")
    @WithUserDetails("admin")
    void t2() throws Exception {
        long id = 1;

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/members/" + id)
                )
                .andDo(print()); // 응답을 출력합니다.

        Member member = memberService.findById(id).get();

        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.username").value(member.getUsername()));
    }

    @Test
    @DisplayName("다건조회, without permission")
    @WithUserDetails("user1")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/members")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("단건조회, without permission")
    @WithUserDetails("user1")
    void t4() throws Exception {
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/members/" + id)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("권한이 없습니다."));
    }
}
