package com.back.global.Rq;

import com.back.domain.member.member.entity.Member;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class Rq {

    public Member getActor() {
        return null;
    }
}
