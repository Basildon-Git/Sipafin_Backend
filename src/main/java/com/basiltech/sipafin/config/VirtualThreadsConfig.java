package com.basiltech.sipafin.config;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class VirtualThreadsConfig implements WebMvcConfigurer, AsyncConfigurer {

    private ExecutorService vtExecutorService;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService virtualThreadExecutorService() {
        if (vtExecutorService == null) {
            ThreadFactory factory = Thread.ofVirtual().name("vt-", 1).factory();
            vtExecutorService = Executors.newThreadPerTaskExecutor(factory);
        }
        return vtExecutorService;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor());
        configurer.setDefaultTimeout(TimeUnit.SECONDS.toMillis(60));
    }

    @Bean
    public AsyncTaskExecutor mvcTaskExecutor() {
        // Bridge ExecutorService -> AsyncTaskExecutor
        return new SimpleAsyncTaskExecutor("mvc-vt-") {
            @Override
            public void execute(Runnable task) {
                virtualThreadExecutorService().execute(task);
            }
        };
    }

    @Override
    public Executor getAsyncExecutor() {
        return virtualThreadExecutorService();
    }

    @PreDestroy
    public void shutdown() {
        if (vtExecutorService != null) {
            vtExecutorService.shutdown();
        }
    }
}
