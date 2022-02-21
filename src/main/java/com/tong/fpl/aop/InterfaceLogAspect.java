package com.tong.fpl.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.UUID;

/**
 * Create by tong on 2022/2/17
 */
@Slf4j
@Aspect
@Configuration
public class InterfaceLogAspect {

    @Pointcut(value = "execution(public * com.tong.fpl.service.IInterfaceService*.*(..))")
    public void interfaceLog() {
    }

    @Around(value = "interfaceLog()")
    public Object around(ProceedingJoinPoint point) {
        MDC.put("uuid", UUID.randomUUID().toString());
        log.info("start fetch data from server, method:{}, args:{}", point.getSignature().getName(), Arrays.toString(point.getArgs()));
        long startTime = System.currentTimeMillis();
        Object object = null;
        try {
            object = point.proceed();
        } catch (Throwable throwable) {
            log.error("fetch data from server, method:{}, error:{}", point.getSignature().getName(), throwable.getMessage());
            throwable.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        log.info("end fetch data from server, method:{}, time escaped:{} ms", point.getSignature().getName(), (endTime - startTime) / 1000);
        return object;
    }

}
