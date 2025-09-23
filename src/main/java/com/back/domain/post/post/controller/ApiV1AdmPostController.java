package com.back.domain.post.post.controller;


import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.dto.AdmPostCountResBody;
import com.back.domain.post.post.service.PostService;
import com.back.global.Rq.Rq;
import com.back.global.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
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
    private final Rq rq;

    @Transactional(readOnly = true)
    @GetMapping("/count")
    @Operation(summary = "글 갯수")
    public AdmPostCountResBody count() {
        Member actor = rq.getActor();

        if (!actor.isAdmin()) {
            throw new ServiceException("403-1", "권한이 없습니다.");
        }
        return new AdmPostCountResBody(postService.count());
    }
}
