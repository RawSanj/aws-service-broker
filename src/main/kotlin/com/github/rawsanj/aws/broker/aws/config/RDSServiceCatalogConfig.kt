package com.github.rawsanj.aws.broker.aws.config

import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import org.springframework.cloud.servicebroker.model.catalog.Plan
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RDSServiceCatalogConfig {

    @Bean(name = arrayOf("rdsServiceDefinition"))
    fun rdsServiceDefinition() : ServiceDefinition {

        val smallPlan = Plan.builder()
                .id(AwsConstants.RDS_DB_T2_MICRO_PLAN)
                .name(AwsConstants.RDS_DB_T2_MICRO_PLAN)
                .description("A db.t2.micro RDS Instance")
                .free(true)
                .build()

        val mediumPlan = Plan.builder()
                .id(AwsConstants.RDS_DB_T2_MEDIUM_PLAN)
                .name(AwsConstants.RDS_DB_T2_MEDIUM_PLAN)
                .description("A db.t2.medium RDS Instance")
                .free(true)
                .build()

        val largePlan = Plan.builder()
                .id(AwsConstants.RDS_DB_T2_LARGE_PLAN)
                .name(AwsConstants.RDS_DB_T2_LARGE_PLAN)
                .description("A db.t2.large RDS Instance")
                .free(true)
                .build()

        val serviceDefinition = ServiceDefinition.builder()
                .id(AwsConstants.RDS_SERVICE_ID)
                .name("rds-db-service")
                .description("An AWS Service to provision RDS Instance."+
                        "To customize RDS Instance provide the following parameters in request: " +
                        "${AwsConstants.RDS_ENGINE_STRING}, ${AwsConstants.DB_ALLOCATED_STORAGE_STRING}, ${AwsConstants.MASTER_USERNAME_STRING}, ${AwsConstants.MASTER_PASSWORD_STRING}")
                .bindable(true)
                .tags("aws", "rds", "database")
                .plans(smallPlan, mediumPlan, largePlan)
                .metadata("displayName", "rds-service-broker")
                .metadata("longDescription", "An AWS Service to provision RDS Instance")
                .metadata("providerDisplayName", "AWS RDS Database Service Provider")
                .build()

        return serviceDefinition
    }

}