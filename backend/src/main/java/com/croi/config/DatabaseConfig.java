package com.croi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableTransactionManagement
@EnableAsync
public class DatabaseConfig {
}
