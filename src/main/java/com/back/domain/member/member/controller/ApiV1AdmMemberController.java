package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberWithUsernameDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/adm/members")
@RequiredArgsConstructor
@Tag(name="ApiV1AdmMemberController", description = "관리자용 API 맴버 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1AdmMemberController {
    private final MemberService memberService;

    @GetMapping
    public List<MemberWithUsernameDto> getItems() {
        List<Member> members = memberService.findAll();

        return members.stream()
                .map(MemberWithUsernameDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public MemberWithUsernameDto getItem(@PathVariable Long id) {
        Member member = memberService.findById(id).get();

        return new MemberWithUsernameDto(member);
    }

}
