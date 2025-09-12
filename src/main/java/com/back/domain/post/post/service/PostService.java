package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    public Post findByTitle(String title) {
        return postRepository.findByTitle(title).get();
    }

    public long count() {
        return postRepository.count();
    }

    public Post create(Member author, String title, String content) {
        Post post = new Post(author, title, content);

        return postRepository.save(post);
    }

    public void update(Post post, String title, String content) {
        post.modify(title, content);
    }

    public List<Post> getList() {
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        return postRepository.findById(id).get();
    }

    public void createComment(Member author, Post post, String content) {
        // Post.addComment 내부에서 자식(PostComment)에 this(post)를 세팅
        // cascade=PERSIST 옵션 덕분에 부모 저장 시 자식도 함께 INSERT 가능
        post.addComment(author, content);
    }

    public boolean deleteComment(Post post, PostComment postComment) {
        // 여기서 deleteComment는 단순히 post.getComments().remove(postComment) 호출
        // 하지만 Post.comments 매핑에 orphanRemoval=true가 걸려있기 때문에
        // 컬렉션에서 제거된 PostComment는 "고아 객체"로 간주 → flush/commit 시 DELETE SQL 실행
        // 즉, 별도로 postCommentRepository.delete(...)를 호출하지 않아도 DB에서 삭제됨
        return post.deleteComment(postComment);
    }

    public void modifyComment(PostComment postComment, String content) {
        // 자식 엔티티(PostComment)의 필드 변경 → 더티 체킹에 의해 UPDATE 실행
        postComment.modify(content);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public Optional<Post> findLatest() {
        return postRepository.findFirstByOrderByIdDesc();
    }

    public PostComment writeComment(Member author, Post post, String content) {
        return post.addComment(author, content);
    }

    public void flush(){
        postRepository.flush();
    }
}
