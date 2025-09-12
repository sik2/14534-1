package com.back.global.aspect;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect // AOP를 사용해 공통 로직을 주입하는 클래스임을 명시
@Component // 스프링 빈으로 등록
public class ResponseAspect {
    // 스프링이 제공하는 HttpServletResponse 객체 (응답 조작에 사용)
    private final HttpServletResponse response;

    // 생성자 주입
    public ResponseAspect(HttpServletResponse response) {
        this.response  = response;
    }

    /**
     * @Around: 지정된 컨트롤러 메서드를 가로채어 실행 전후에 로직을 실행
     * - 대상 메서드를 '둘러싸고(around)' 실행 전·후를 모두 감시/제어한다
     *
     * 포인트컷:
     * - @RestController 안의 메서드이면서
     *   @GetMapping, @PostMapping, @PutMapping, @DeleteMapping, @RequestMapping 이 붙은 경우
     * - 또는 @ResponseBody가 붙은 경우
     *
     * 즉, REST API 응답을 반환하는 모든 컨트롤러 메서드를 대상으로 함
     */
    @Around("""
                execution(public com.back.global.rsData.RsData *(..)) &&
                (
                    within(@org.springframework.stereotype.Controller *) ||
                    within(@org.springframework.web.bind.annotation.RestController *)
                ) &&
                (
                    @annotation(org.springframework.web.bind.annotation.GetMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PostMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PutMapping) ||
                    @annotation(org.springframework.web.bind.annotation.DeleteMapping) ||
                    @annotation(org.springframework.web.bind.annotation.RequestMapping)
                )
            """)
    public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // 원래 컨트롤러 메서드 실행 (ex: write() 메서드 실행)
        Object proceed = joinPoint.proceed();

        // 반환값이 RsData라면, 그 안에 있는 statusCode를 HttpServletResponse에 반영
        RsData<?> rsData = (RsData<?>) proceed;
        response.setStatus(rsData.statusCode());

        // 응답 본문은 그대로 클라이언트에게 전달
        return proceed;
    }
}