package com.re.rikkei_bank_manager.audit.aspect;

import com.re.rikkei_bank_manager.audit.entity.AuditLog;
import com.re.rikkei_bank_manager.audit.service.AuditLogService;
import com.re.rikkei_bank_manager.common.enums.TransactionStatus;
import com.re.rikkei_bank_manager.common.util.SecurityUtils;
import com.re.rikkei_bank_manager.transaction.dto.request.TransferRequest;
import com.re.rikkei_bank_manager.transaction.dto.response.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect @Component @RequiredArgsConstructor
public class AuditAspect {
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    private final AuditLogService auditLogService;

    @Pointcut("execution(* com.re.rikkei_bank_manager.transaction.service.TransactionService.transfer(..))")
    public void transferPointcut() {}

    @Before("transferPointcut()")
    public void startTimer() {
        START_TIME.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "transferPointcut()", returning = "result")
    public void success(JoinPoint jp, Object result) {
        TransferRequest req = extract(jp);
        TransferResponse res = result instanceof TransferResponse tr ? tr : null;
        auditLogService.save(AuditLog.builder()
                .username(username()).action("TRANSFER")
                .fromAccountNumber(req == null ? null : req.getFromAccountNumber())
                .toAccountNumber(req == null ? null : req.getToAccountNumber())
                .amount(req == null ? null : req.getAmount())
                .status(TransactionStatus.SUCCESS)
                .message(res == null ? "Transfer successful" : "Transfer successful: " + res.getTransactionCode())
                .executionTimeMs(elapsed())
                .build());
        START_TIME.remove();
    }

    @AfterThrowing(pointcut = "transferPointcut()", throwing = "ex")
    public void failed(JoinPoint jp, Throwable ex) {
        TransferRequest req = extract(jp);
        auditLogService.save(AuditLog.builder()
                .username(username()).action("TRANSFER")
                .fromAccountNumber(req == null ? null : req.getFromAccountNumber())
                .toAccountNumber(req == null ? null : req.getToAccountNumber())
                .amount(req == null ? null : req.getAmount())
                .status(TransactionStatus.FAILED)
                .message("Transfer failed: " + ex.getMessage())
                .executionTimeMs(elapsed())
                .build());
        START_TIME.remove();
    }

    private TransferRequest extract(JoinPoint jp) {
        for (Object arg : jp.getArgs()) if (arg instanceof TransferRequest req) return req;
        return null;
    }

    private String username() {
        String u = SecurityUtils.getCurrentUsername();
        return u == null ? "anonymous" : u;
    }

    private Long elapsed() {
        Long start = START_TIME.get();
        return start == null ? null : System.currentTimeMillis() - start;
    }
}
