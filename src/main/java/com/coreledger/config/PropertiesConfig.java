package com.coreledger.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CoreLedgerProperties.class)
public class PropertiesConfig {
}
