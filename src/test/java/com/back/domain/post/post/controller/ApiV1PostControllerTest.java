package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test") // 테스트 환경에서는 test 프로파일을 활성화합니다.
@SpringBootTest // 스프링부트 테스트 클래스임을 나타냅니다.
@AutoConfigureMockMvc // MockMvc를 자동으로 설정합니다.
@Transactional // 각 테스트 메서드가 종료되면 롤백됩니다.
public class ApiV1PostControllerTest {
    @Autowired
    private MockMvc mvc; // MockMvc를 주입받습니다.

    @Autowired
    private PostService postService;
    @Autowired
    private MemberService memberService;

    //글쓰기 테스트
    @Test
    @DisplayName("글 쓰기")
    @WithUserDetails("user1")
    void t1() throws Exception {
        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": "내용"
                                        }
                                        """)
                )
                .andDo(print()); // 응답을 출력합니다.

        Post post = postService.findLatest().get();

        // 201 Created 상태코드 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 게시글이 생성되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.content").value("내용"));

    }

    //글쓰기 제목 누락 테스트
    @Test
    @DisplayName("글 쓰기 400 - 제목 누락")
    @WithUserDetails("user1")
    void t7() throws Exception {

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "",
                                            "content": "내용"
                                        }
                                        """)
                )
                .andDo(print()); // 응답을 출력합니다.

        // 400 BadRequest 상태코드 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        title-NotBlank-must not be blank
                        title-Size-size must be between 2 and 100
                        """.stripIndent().trim()));
    }

    //글쓰기 내용 누락 테스트
    @Test
    @DisplayName("글 쓰기 400 - 내용 누락")
    @WithUserDetails("user1")
    void t8() throws Exception {

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": ""
                                        }
                                        """)
                )
                .andDo(print()); // 응답을 출력합니다.

        // 400 BadRequest 상태코드 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content-NotBlank-must not be blank
                        content-Size-size must be between 2 and 2000
                        """.stripIndent().trim()));
    }

    //글쓰기 JSON 문법 에러 테스트
    @Test
    @DisplayName("글 쓰기 400 - JSON 문법 에러")
    @WithUserDetails("user1")
    void t9() throws Exception {

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목",
                                            content": "내용"
                                        }
                                        """)
                )
                .andDo(print()); // 응답을 출력합니다.

        // 400 BadRequest 상태코드 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("요청 본문 형식이 올바르지 않습니다."));
    }


    // 권한 검증 헤더 누락
    @Test
    @DisplayName("글 쓰기, 누락 authorization header")
    void t10() throws Exception {
        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": "내용"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("글 쓰기, 잘못된 authorization header")
    void t11() throws Exception {
        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "wrong-api-key")
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": "내용"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-2"))
                .andExpect(jsonPath("$.msg").value("인증 정보가 올바르지 않습니다."));
    }


    @Test
    @DisplayName("글 쓰기, 잘못된 authorization header")
    void t12() throws Exception {
        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer wrong-api-key")
                                .content("""
                                        {
                                            "title": "제목",
                                            "content": "내용"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-3"))
                .andExpect(jsonPath("$.msg").value("회원을 찾을 수 없습니다."));
    }

    //글 수정 테스트
    @Test
    @DisplayName("글 수정")
    void t2() throws Exception {
        long id = 1;

        Post post = postService.findById(id);

        String apiKey = post.getAuthor().getApiKey();

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + apiKey)
                                .content("""
                                        {
                                            "title": "제목 update",
                                            "content": "내용 update"
                                        }
                                        """)
                )
                .andDo(print()); // 응답을 출력합니다.
        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 게시글이 수정되었습니다.".formatted(id)));

//        Post post = postService.findById(id);
//
//        assertThat(post.getTitle().equals("제목 update"));
//        assertThat(post.getContent().equals("내용 update"));
    }

    @Test
    @DisplayName("글 수정, without permission")
    @WithUserDetails("user2")
    void t13() throws Exception {
        long id = 1;

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 update",
                                            "content": "내용 update"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 수정 권한이 없습니다.".formatted(id)));
    }


    @Test
    @DisplayName("글 삭제")
    void t3() throws Exception {
        long id = 1;

        Post post = postService.findById(id);

        String apiKey = post.getAuthor().getApiKey();

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/" + id)
                        .header("Authorization", "Bearer " + apiKey)
                )

                .andDo(print()); // 응답을 출력합니다.

        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 게시글이 삭제되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("글 삭제, without permission")
    @WithUserDetails("user2")
    void t14() throws Exception {
        long id = 1;
        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/" + id)
                )

                .andDo(print()); // 응답을 출력합니다.

        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 삭제 권한이 없습니다.".formatted(id)));
    }

    @Test
    @DisplayName("글 단건조회")
    void t4() throws Exception {
        long id = 1;

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/" + id)
                )
                .andDo(print()); // 응답을 출력합니다.

        Post post = postService.findById(id);

        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.authorName").value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").value(post.getContent()));
    }

    @Test
    @DisplayName("글 단건조회, 404")
    void t6() throws Exception {
        long id = Integer.MAX_VALUE;

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/" + id)
                )
                .andDo(print()); // 응답을 출력합니다.

        // 404 NotFound상태코드 검증
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"));
    }


    @Test
    @DisplayName("글 다건조회")
    void t5() throws Exception {

        //요청을 보냅니다.
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts")
                )
                .andDo(print()); // 응답을 출력합니다.

        List<Post> posts = postService.getList();


        // 200 Ok 상태코드 검증
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(posts.size()));

        for (int i =0; i < posts.size(); i++) {
            Post post = posts.get(i);

            resultActions
                .andExpect(jsonPath("$[%d].id".formatted(i)).value(post.getId()))
                .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$[%d].authorName".formatted(i)).value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$[%d].title".formatted(i)).value(post.getTitle()))
                .andExpect(jsonPath("$[%d].content".formatted(i)).value(post.getContent()));
        }
    }
}
