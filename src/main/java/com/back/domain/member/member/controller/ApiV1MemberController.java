package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberJoinReqBody;
import com.back.domain.member.member.dto.MemberLoginReqBody;
import com.back.domain.member.member.dto.MemberLoginResBody;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.Rq.Rq;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name="ApiV1MemberController", description = "API 맴버 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @Transactional
    @PostMapping
    @Operation(summary = "회원가입")
    public RsData<MemberDto> join(@Valid @RequestBody MemberJoinReqBody reqBody) {
        Member member = memberService.join(reqBody.username(), reqBody.password(), reqBody.nickname());

        return new RsData<>(
                "201-1",
                "%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getNickname()),
                new MemberDto(member)
        );
    }

    @Transactional
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public RsData<MemberLoginResBody> login(
            @Valid @RequestBody MemberLoginReqBody reqBody
    ) {
        Member member = memberService.findByUsername(reqBody.username())
                .orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 회원입니다."));

        memberService.checkPassword(
                member,
                reqBody.password()
        );

        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("apiKey", member.getApiKey());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getNickname()),
                new MemberLoginResBody(
                        new MemberDto(member),
                        member.getApiKey(),
                        accessToken
                )
            );

    }

    @Transactional(readOnly = true)
    @GetMapping("/me")
    @Operation(summary = "내 정보")
    public RsData<MemberDto> me() {
        Member actor = rq.getActor();
        // 실시간성을 보장하기위해 DB 조회
        Member member  = memberService.findById(actor.getId()).get();

        return new RsData(
                "200-1",
                "%s님 정보입니다.".formatted(actor.getNickname()),
                new MemberDto(member)
        );
    }

    @Transactional
    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    public RsData<Void> logout() {

        rq.deleteCookie("apiKey");

        return new RsData<>(
                "200-1",
                "로그아웃 되었습니다."
        );
    }
}
