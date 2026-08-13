package com.coreledger.aspect;

import com.coreledger.annotation.Audit;
import com.coreledger.common.api.ApiResponse;
import com.coreledger.common.util.RequestIdHolder;
import com.coreledger.dto.request.LoginRequest;
import com.coreledger.dto.request.RegisterRequest;
import com.coreledger.dto.response.UserResponse;
import com.coreledger.entity.AuditLog;
import com.coreledger.entity.User;
import com.coreledger.mapper.AuditLogMapper;
import com.coreledger.mapper.UserMapper;
import com.coreledger.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final AuditLogMapper auditLogMapper;
    private final UserMapper userMapper;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        String result = "SUCCESS";
        Object response = null;
        try {
            response = joinPoint.proceed();
            return response;
        } catch (Throwable ex) {
            result = "FAIL";
            throw ex;
        } finally {
            try {
                saveAuditLog(audit, result, joinPoint.getArgs(), response);
            } catch (RuntimeException ex) {
                log.warn("Audit log save failed operation={}", audit.operation());
            }
        }
    }

    private void saveAuditLog(Audit audit, String result, Object[] args, Object response) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        AuditUser auditUser = auditUser(args, response);

        AuditLog logEntry = new AuditLog();
        logEntry.setUserId(auditUser.userId());
        logEntry.setUsername(auditUser.username());
        logEntry.setOperation(audit.operation());
        logEntry.setResource(audit.resource());
        logEntry.setRequestMethod(request == null ? null : request.getMethod());
        logEntry.setRequestUri(request == null ? null : request.getRequestURI());
        logEntry.setRequestIp(request == null ? null : request.getRemoteAddr());
        logEntry.setRequestId(RequestIdHolder.get());
        logEntry.setResult(result);
        logEntry.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(logEntry);
    }

    private AuditUser auditUser(Object[] args, Object response) {
        CurrentUser currentUser = currentUser();
        if (currentUser != null) {
            return new AuditUser(currentUser.id(), currentUser.username());
        }
        if (response instanceof ApiResponse<?> apiResponse && apiResponse.data() instanceof UserResponse userResponse) {
            return new AuditUser(userResponse.id(), userResponse.username());
        }
        for (Object arg : args) {
            if (arg instanceof LoginRequest loginRequest) {
                User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginRequest.username()));
                return new AuditUser(user == null ? null : user.getId(), loginRequest.username());
            }
            if (arg instanceof RegisterRequest registerRequest) {
                return new AuditUser(null, registerRequest.username());
            }
        }
        return new AuditUser(null, null);
    }

    private CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser user) {
            return user;
        }
        return null;
    }

    private record AuditUser(Long userId, String username) {
    }
}
