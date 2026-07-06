package com.mailbatch.mailbatchsystem.exception;

import com.mailbatch.mailbatchsystem.dto.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;

/**
 * 全局异常处理
 * 统一处理所有异常，返回统一的Result格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常（@Valid注解触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder sb = new StringBuilder("参数校验失败：");
        fieldErrors.forEach(fieldError -> 
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ")
        );
        String message = sb.toString();
        log.warn("参数校验失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理绑定异常（表单验证失败）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder sb = new StringBuilder("参数绑定失败：");
        fieldErrors.forEach(fieldError -> 
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ")
        );
        String message = sb.toString();
        log.warn("参数绑定失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理约束违反异常（@Validated注解触发）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder sb = new StringBuilder("参数校验失败：");
        violations.forEach(violation -> 
            sb.append(violation.getMessage()).append("; ")
        );
        String message = sb.toString();
        log.warn("参数校验失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理认证异常（登录失败）
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.unauthorized("用户名或密码错误");
    }

    /**
     * 处理凭证错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("凭证错误: {}", e.getMessage());
        return Result.unauthorized("用户名或密码错误");
    }

    /**
     * 处理访问拒绝异常（权限不足）
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return Result.forbidden("权限不足，无法访问该资源");
    }

    /**
     * 处理业务异常（自定义异常）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    /**
     * 处理其他未预期的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统内部错误，请联系管理员");
    }
}
