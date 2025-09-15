package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberJoinReqBody;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name="ApiV1MemberController", description = "API 맴버 컨트롤러")
public class ApiV1MemberController {
    private final MemberService memberService;

    @PostMapping
    public RsData<MemberDto> join(@Valid @RequestBody MemberJoinReqBody reqBody) {

        memberService.findByUsername(reqBody.username())
                .ifPresent(_member -> {
                    throw new ServiceException("409-1", "이미 존재하는 회원입니다.");
                });

        Member member = memberService.join(reqBody.username(), reqBody.password(), reqBody.nickname());


        return new RsData<>(
                "201-1",
                "%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getNickname()),
                new MemberDto(member)
        );
    }
}
