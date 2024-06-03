package org.spring.oneplusone.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
public class TaskConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler(); // 단일 스레드를 사용하는 스케줄러
    }
}
