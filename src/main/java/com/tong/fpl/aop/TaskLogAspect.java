package com.tong.fpl.aop;

import com.tong.fpl.log.TaskLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Create by tong on 2021/9/2
 */
@Aspect
@Component
public class TaskLogAspect {

	@Pointcut("execution(public * com.tong.fpl.task.*.*(..))")
	public void taskLog() {
	}

	@Around(value = "taskLog()")
	public Object around(ProceedingJoinPoint point) {
		MDC.put("uuid", UUID.randomUUID().toString());
		TaskLog.info("start task:{}", point.getSignature().getName(), Arrays.toString(point.getArgs()));
		long startTime = System.currentTimeMillis();
		Object object = null;
		try {
			object = point.proceed();
		} catch (Throwable throwable) {
			TaskLog.error("task:{}, error:{}", point.getSignature().getName(), throwable.getMessage());
			throwable.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		TaskLog.info("end task:{}, time escaped:{} ms", point.getSignature().getName(), (endTime - startTime) / 1000);
		return object;
	}

}
