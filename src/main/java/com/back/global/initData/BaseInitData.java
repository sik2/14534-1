package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Configuration
public class BaseInitData {

    @Autowired
    @Lazy
    private BaseInitData self;

    private final PostService postService;
    private final MemberService memberService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
//            self.work3();
        };

    }

    @Transactional
    public void work1() {
        if (memberService.count() > 0) return;
        
        Member memberSystem = memberService.join("system", "1234", "시스템");
        if (AppConfig.isNotProd()) memberSystem.modifyApiKey(memberSystem.getUsername());

        Member memberAdmin = memberService.join("admin", "1234", "관리자");
        if (AppConfig.isNotProd()) memberAdmin.modifyApiKey(memberAdmin.getUsername());

        Member memberUser1 = memberService.join("user1", "1234", "유저1");
        if (AppConfig.isNotProd()) memberUser1.modifyApiKey(memberUser1.getUsername());

        Member memberUser2 = memberService.join("user2", "1234", "유저2");
        if (AppConfig.isNotProd()) memberUser2.modifyApiKey(memberUser2.getUsername());

        Member memberUser3 = memberService.join("user3", "1234", "유저3");
        if (AppConfig.isNotProd()) memberUser3.modifyApiKey(memberUser3.getUsername());
    }

    @Transactional
    public void work2() {
        if (postService.count() > 0) return;

        Member memberUser1 = memberService.findByUsername("user1").get();
        Member memberUser2 = memberService.findByUsername("user2").get();
        Member memberUser3 = memberService.findByUsername("user3").get();

        Post post1 = postService.create(memberUser1, "제목 1", "내용 1");
        Post post2 = postService.create(memberUser1, "제목 2", "내용 2");
        Post post3 = postService.create(memberUser2,"제목 3", "내용 3");
        Post post4 = postService.create(memberUser2,"제목 4", "내용 4");
        Post post5 = postService.create(memberUser2,"제목 5", "내용 5");

        post1.addComment(memberUser1,"댓글 1-1");
        post1.addComment(memberUser1,"댓글 1-2");
        post1.addComment(memberUser2,"댓글 1-3");
        post2.addComment(memberUser3,"댓글 2-1");
        post2.addComment(memberUser3,"댓글 2-2");
        post3.addComment(memberUser3,"댓글 3-1");
        post3.addComment(memberUser3,"댓글 3-2");
    }

    @Transactional
    public void work3() {

    }
}
