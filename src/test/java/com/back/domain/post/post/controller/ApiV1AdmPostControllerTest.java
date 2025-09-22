package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1AdmPostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("글 갯수 통계")
    void t1() throws Exception {
        Member actor = memberService.findByUsername("admin").get();
        String actorApiKey = actor.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/posts/count")
                                .header("Authorization", "Bearer " + actorApiKey)
                )
                .andDo(print());

        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1AdmPostController.class))
                .andExpect(handler().methodName("count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.all").value(postService.count()));
    }
}
