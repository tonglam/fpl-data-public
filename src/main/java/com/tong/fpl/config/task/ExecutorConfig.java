package com.tong.fpl.config.task;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Create by tong on 2021/10/26
 */
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean(name = "TaskThreadPool")
    public Executor MyThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setQueueCapacity(100);
        executor.setMaxPoolSize(10);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("TaskThreadPool-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

}
