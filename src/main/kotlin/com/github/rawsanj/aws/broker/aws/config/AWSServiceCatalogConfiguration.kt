package com.github.rawsanj.aws.broker.aws.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.servicebroker.model.catalog.Catalog
import org.springframework.cloud.servicebroker.model.catalog.Plan
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AWSServiceCatalogConfiguration {

    @Bean
    fun catalog(@Qualifier("rdsServiceDefinition") rdsServiceDefinition: ServiceDefinition,
                @Qualifier("s3ServiceDefinition") s3ServiceDefinition: ServiceDefinition): Catalog {

        return Catalog.builder()
                .serviceDefinitions(rdsServiceDefinition, s3ServiceDefinition)
                .build()
    }

}