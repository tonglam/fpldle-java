package com.tong.fpldle.aop;

import com.tong.fpldle.config.log.ControllerLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Create by tong on 2022/2/17
 */
@Aspect
@Component
public class ControllerLogAspect {

    ThreadLocal<Long> startTime = new ThreadLocal<>();
    StringBuffer stringBuffer;

    @Pointcut("execution(public * com.tong.fpldle.controller.*.*(..))")
    public void controllerLog() {
    }

    @Before("controllerLog()")
    public void doBefore(JoinPoint joinPoint) {
        this.startTime.set(System.currentTimeMillis());
        this.stringBuffer = new StringBuffer();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            // ip
            HttpServletRequest request = attributes.getRequest();
            // params
            this.stringBuffer.append("url:{").append(request.getRequestURI()).append("}");
            this.stringBuffer.append(", method:{").append(request.getMethod()).append("}");
            this.stringBuffer.append(", args:{").append(Arrays.toString(joinPoint.getArgs())).append("}");
        }
    }

    @After("controllerLog()")
    public void doAfter() {
    }

    @AfterReturning(returning = "obj", pointcut = "controllerLog()")
    public void doAfterReturning(Object obj) {
        this.stringBuffer.append(", response:{data}");
        this.stringBuffer.append(", elapsed time:").append(System.currentTimeMillis() - this.startTime.get()).append("ms!");
        ControllerLog.info(this.stringBuffer.toString());
    }

}
