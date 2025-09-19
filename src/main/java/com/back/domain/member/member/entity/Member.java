package com.back.domain.member.member.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@ToString
@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {
    @Column(unique = true)
    private String username;
    private String password;
    private String nickname;
    @Column(unique = true)
    private String apiKey;

    public Member (long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Member(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.apiKey = UUID.randomUUID().toString();
    }

    public void modifyApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
