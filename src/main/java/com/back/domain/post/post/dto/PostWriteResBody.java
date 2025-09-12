package com.back.domain.post.post.dto;

public record PostWriteResBody (
        long totalCount,
        PostDto post
) {
}
