package com.enerlytics.usages.configs;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InfluxDBProperties.class)
public class InfluxDBConfig {

    @Bean(destroyMethod = "close")
    public InfluxDBClient influxDBClient(InfluxDBProperties properties) {
        return InfluxDBClientFactory.create(properties.url(), properties.token().toCharArray(), properties.org());
    }
}
