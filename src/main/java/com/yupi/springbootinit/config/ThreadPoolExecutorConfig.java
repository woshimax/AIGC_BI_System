package com.yupi.springbootinit.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {
    ThreadFactory threadFactory = new ThreadFactory(){
        //正常来说这个工厂都没必要写，这里只是为了调试输出线程信息
        private int count = 1;
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("线程" + count);
            count++;
            return thread;
        }
    };
    //创建一个配置好的线程池
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,
                2,
                60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(6),
                threadFactory);
        return threadPoolExecutor;
    }
}
