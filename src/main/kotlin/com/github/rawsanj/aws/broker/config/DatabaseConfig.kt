package com.github.rawsanj.aws.broker.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment


@Configuration
@Profile("cloudFoundry")
class CloudFoundryDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("cloudFoundry.datasource")
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @ConfigurationProperties("cloudFoundry.datasource")
    fun dataSource(properties: DataSourceProperties): HikariDataSource {
        return properties.initializeDataSourceBuilder().type(HikariDataSource::class.java)
                .build()
    }

}

@Configuration
@Profile("kubernetes")
class KubernetesDatabaseConfig(val env: Environment) {

    @Bean
    @Primary
    @ConfigurationProperties("kubernetes.datasource")
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @ConfigurationProperties("kubernetes.datasource")
    fun dataSource(properties: DataSourceProperties): HikariDataSource {
        return properties.initializeDataSourceBuilder().type(HikariDataSource::class.java)
                .build()
    }

}