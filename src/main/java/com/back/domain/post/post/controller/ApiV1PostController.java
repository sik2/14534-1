package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.dto.PostModifyReqBody;
import com.back.domain.post.post.dto.PostWriteReqBody;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // @Controller + @ResponseBody
@Validated
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name="ApiV1PostController", description = "API 글 컨트롤러")
public class ApiV1PostController {
    private final PostService postService;
    private final MemberService memberService;

    @Transactional(readOnly = true)
    @GetMapping
    @Operation(summary = "다건 조회")
    public List<PostDto> getItems() {
        List<Post> items = postService.getList();

        return items
                .stream()
                .map(PostDto::new) // postDto 변환
                .toList();
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @Operation(summary = "단건 조회")
    public PostDto getItem(@PathVariable Long id) {
        Post item = postService.findById(id);

        return new PostDto(item);
    }

    @Transactional
    @DeleteMapping("/{id}")
    @Operation(summary = "삭제")
    public RsData<PostDto> delete(
            @PathVariable Long id,
            @NotBlank @Size(min = 2, max = 50) @RequestHeader("Authorization") String authorization
    ) {
        String apiKey = authorization.replace("Bearer ", "");

        Member author = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 회원입니다."));


        Post post = postService.findById(id);

        if (!author.equals(post.getAuthor())) {
            throw new ServiceException("403-1", "글 삭제 권한이 없습니다.");
        }

        postService.delete(post);

        return new RsData<>("200-1", "%d번 게시글이 삭제되었습니다.".formatted(id), new PostDto(post));
    }

    @PostMapping
    @Transactional
    @Operation(summary = "작성")
    public RsData<PostDto> write(
            @Valid @RequestBody PostWriteReqBody reqBody,
            @NotBlank @Size(min = 2, max = 50) @RequestHeader("Authorization") String authorization
    ) {
        String apiKey = authorization.replace("Bearer ", "");

        Member author = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 회원입니다."));

        Post post = postService.create(author, reqBody.title(), reqBody.content());

        return new RsData<>(
                        "201-1",
                        "%d번 게시글이 생성되었습니다.".formatted(post.getId()),
                        new PostDto(post)
                );
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
    public RsData<Void> modify(
            @PathVariable long id,
            @Valid @RequestBody PostModifyReqBody reqBody,
            @NotBlank @Size(min = 2, max = 50) @RequestHeader("Authorization") String authorization

    ) {
        String apiKey = authorization.replace("Bearer ", "");

        Member author = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 회원입니다."));

        Post post = postService.findById(id);
        postService.update(post, reqBody.title(), reqBody.content());

        if (!author.equals(post.getAuthor())) {
            throw new ServiceException("403-1", "글 수정 권한이 없습니다.");
        }

        return new RsData<>(
                "200-1",
                "%d번 게시글이 수정되었습니다.".formatted(id)
                );
    }
}
