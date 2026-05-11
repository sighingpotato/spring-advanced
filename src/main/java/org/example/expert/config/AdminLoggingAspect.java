package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminLoggingAspect {
    // 객체를 JSON으로 변환하기 위해 사용
    private final ObjectMapper objectMapper;

    // 두 컨트롤러를 감시하라는 뜻
    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..)) || " +
              "execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    public void adminApi() {}

    @Around("adminApi()")
    public Object logAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 요청 정보 추출
        Long userId = (Long) request.getAttribute("userId");
        String url = request.getRequestURI();
        LocalDateTime requestTime = LocalDateTime.now();

        // 요청 본문 추출
        Object[] args = joinPoint.getArgs();
        String requestBody = "없음";
        for (Object arg : args) {
            // dto인 경우 json으로 변환
            if (arg != null && arg.getClass().getSimpleName().contains("Request")) {
                requestBody = objectMapper.writeValueAsString(arg);
                break;
            }
        }

        log.info("[Admin API Request] Time: {}, UserID: {}, URL: {}, RequestBody: {}", requestTime, userId, url, requestBody);

        // 실제 컨트롤러 메서드 실행
        Object result = joinPoint.proceed();

        // 응답 본문 로깅
        String responseBody = result != null ? objectMapper.writeValueAsString(result) : "없음";
        log.info("[Admin API Request] Time: {}, UserID: {}, URL: {}, RequestBody: {}", LocalDateTime.now(), userId, url, requestBody);

        return result;
    }
}
