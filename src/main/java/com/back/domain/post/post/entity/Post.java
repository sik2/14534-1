package com.back.domain.post.post.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@Getter
@Entity
public class Post extends BaseEntity {
    @ManyToOne
    private Member author;
    private String title;
    private  String content;

    /**
     * mappedBy = "post" : 관계의 주인은 PostComment.post (자식이 FK 보유)
     * fetch = LAZY        : post.getComments() 접근 시점에 쿼리 실행(지연 로딩)
     * cascade = PERSIST   : 부모 저장 시 컬렉션에 포함된 새 댓글도 함께 persist
     * cascade = REMOVE    : 부모 삭제 시 자식 댓글 전체 자동 삭제
     * orphanRemoval = true: 컬렉션에서 자식을 제거하거나 child.post=null 하면 해당 자식 DELETE
     */
    @OneToMany(mappedBy = "post", fetch = LAZY, cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    public Post(Member author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public void modify(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * addComment
     * - new PostComment(this, content) 로 자식 쪽(owner) 연관관계 설정 (mappedBy 구조하에 중요)
     * - comments.add(...) 로 부모 컬렉션에 포함
     * - 이후 부모를 저장하면 cascade=PERSIST 덕분에 자식도 자동 INSERT
     */
    public PostComment addComment(Member author, String content) {
        PostComment postComment = new PostComment(author, this, content);
        comments.add(postComment);

        return postComment;
    }

    /**
     * findCommentById
     * - LAZY: comments 접근 시점에 필요하면 쿼리 발생
     * - 컬렉션 내에서 id로 필터링 (영속성 컨텍스트/1차 캐시 + 초기화된 컬렉션 범위 내 탐색)
     */
    public Optional<PostComment> findCommentById (long id) {
        return comments
                .stream()
                .filter(comment -> comment.getId() == id)
                .findFirst();
    }

    /**
     * deleteComment
     * - comments.remove(child) 하면 orphanRemoval=true 덕분에 해당 자식이 "고아"로 판단되어 DELETE
     * - 단, 연관관계의 주인은 자식(PostComment.post)이므로 child.setPost(null) 까지 끊어주는 것이 안전
     *   (양방향 헬퍼 관례: add/remove 시 항상 양쪽을 함께 정리)
     * - 부모 자체를 삭제하면 cascade=REMOVE 로 자식 전체가 자동 삭제
     */
    public boolean deleteComment(PostComment postComment) {
        if (postComment == null) return false;

        return comments.remove(postComment);
    }

    public void checkActorCanModify(Member actor) {
        if (!actor.getUsername().equals(author.getUsername())) {
            throw new ServiceException("403-1", "%d번 글 수정 권한이 없습니다.".formatted(getId()));
        }
    }

    public void checkActorCanDelete(Member actor) {
        if (!actor.getUsername().equals(author.getUsername())) {
            throw new ServiceException("403-1", "%d번 글 삭제 권한이 없습니다.".formatted(getId()));
        }
    }
}
