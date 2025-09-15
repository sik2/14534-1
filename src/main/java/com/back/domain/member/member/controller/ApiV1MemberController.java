package com.back.domain.member.member.controller;

import com.back.domain.member.member.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name="ApiV1MemberController", description = "API 맴버 컨트롤러")
public class ApiV1MemberController {
    private final MemberService memberService;
}
