package com.back.domain.post.post.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // @Controller + @ResponseBody
@RequestMapping("/api/v1/adm/posts")
@RequiredArgsConstructor
@Tag(name="ApiV1PostAdmController", description = "관리자용 API 글 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1AdmPostController {
}
