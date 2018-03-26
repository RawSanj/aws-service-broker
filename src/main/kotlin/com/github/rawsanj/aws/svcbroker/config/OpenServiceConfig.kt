package com.github.rawsanj.aws.svcbroker.config

import org.springframework.cloud.servicebroker.model.catalog.Catalog
import org.springframework.cloud.servicebroker.model.catalog.Plan
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition
import org.springframework.context.annotation.Configuration

@Configuration
class OpenServiceConfig {

    fun catalog(): Catalog {
        val smallPlan = Plan.builder()
                .id("small")
                .name("small")
                .description("A small RDS Instance")
                .free(true)
                .build()

        val largePlan = Plan.builder()
                .id("small")
                .name("small")
                .description("A large RDS Instance")
                .free(true)
                .build()

        val serviceDefinition = ServiceDefinition.builder()
                .id("aws-service-broker")
                .name("aws-service-broker")
                .description("An AWS Service to provision RDS Instance")
                .bindable(true)
                .tags("aws", "rds", "database")
                .plans(smallPlan, largePlan)
                .metadata("displayName", "aws-service-broker")
                .metadata("longDescription", "An AWS Service to provision RDS Instance")
                .metadata("providerDisplayName", "Raw AWS Service Provider")
                .build()

        return Catalog.builder()
                .serviceDefinitions(serviceDefinition)
                .build()
    }

}