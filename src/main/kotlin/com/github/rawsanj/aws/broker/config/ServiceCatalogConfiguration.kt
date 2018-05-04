package com.github.rawsanj.aws.broker.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.servicebroker.model.catalog.Catalog
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceCatalogConfiguration {

    @Bean
    fun catalog(@Qualifier("rdsServiceDefinition") rdsServiceDefinition: ServiceDefinition,
                @Qualifier("s3ServiceDefinition") s3ServiceDefinition: ServiceDefinition): Catalog {

        return Catalog.builder()
                .serviceDefinitions(rdsServiceDefinition, s3ServiceDefinition)
                .build()
    }

}