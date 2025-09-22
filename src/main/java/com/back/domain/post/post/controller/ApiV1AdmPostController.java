package com.back.domain.post.post.controller;


import com.back.domain.post.post.dto.AdmPostCountResBody;
import com.back.domain.post.post.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // @Controller + @ResponseBody
@RequestMapping("/api/v1/adm/posts")
@RequiredArgsConstructor
@Tag(name="ApiV1PostAdmController", description = "관리자용 API 글 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1AdmPostController {

    private final PostService postService;

    @GetMapping("/count")
    public AdmPostCountResBody count() {
        return new AdmPostCountResBody(postService.count());
    }
}
