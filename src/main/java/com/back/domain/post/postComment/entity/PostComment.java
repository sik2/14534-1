package com.back.domain.post.postComment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class PostComment extends BaseEntity {
    @ManyToOne
    private Member author;

    private String content;

    @JsonIgnore
    @ManyToOne
    private Post post;

    public PostComment(Member author, Post post, String content) {
        this.author = author;
        this.post = post;
        this.content = content;
    }

    public void modify(String content) {
        this.content = content;
    }

    public void checkActorCanModify(Member actor) {
        if (!actor.getUsername().equals(author.getUsername())) {
            throw new ServiceException("403-1", "%d번 댓글 수정 권한이 없습니다.".formatted(getId()));
        }
    }

    public void checkActorCanDelete(Member actor) {
        if (!actor.getUsername().equals(author.getUsername())) {
            throw new ServiceException("403-1", "%d번 댓글 삭제 권한이 없습니다.".formatted(getId()));
        }
    }
}
