package com.github.rawsanj.aws.broker.aws.config.s3

import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import org.springframework.cloud.servicebroker.model.catalog.Plan
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3ServiceCatalogConfig {

    @Bean(name = arrayOf("s3ServiceDefinition"))
    fun s3ServiceDefinition() : ServiceDefinition {

        val s3bucketPlan = Plan.builder()
                .id(AwsConstants.S3_BUCKET_PLAN)
                .name(AwsConstants.S3_BUCKET_PLAN)
                .description("An S3 Bucket")
                .free(true)
                .build()

        val serviceDefinition = ServiceDefinition.builder()
                .id(AwsConstants.S3_SERVICE_ID)
                .name("s3-bucket-service")
                .description("An AWS Service to provision S3 Buckets")
                .bindable(true)
                .tags("aws", "s3", "bucket", "block-storage")
                .plans(s3bucketPlan)
                .metadata("displayName", "s3-service-broker")
                .metadata("longDescription", "An AWS Service to provision S3 Buckets")
                .metadata("providerDisplayName", "AWS S3 Service Provider")
                .build()

        return serviceDefinition
    }

}