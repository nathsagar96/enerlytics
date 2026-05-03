package com.enerlytics.usages.configs;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfig {

    @Value("${influx.url}")
    private String dbUrl;

    @Value("${influx.token}")
    private String dbToken;

    @Value("${influx.org}")
    private String dbOrg;

    @Bean
    public InfluxDBClient dbClient() {
        return InfluxDBClientFactory.create(dbUrl, dbToken.toCharArray(), dbOrg);
    }
}
