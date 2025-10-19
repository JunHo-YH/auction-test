package org.example.lastcall.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        org.example.lastcall.common.config.MailProperties.class,
})
public class PropertiesConfiguration {
}
