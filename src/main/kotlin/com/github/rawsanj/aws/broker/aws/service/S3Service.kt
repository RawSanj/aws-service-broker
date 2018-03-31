package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*


@Service
class S3Service(val awsCredentialsProvider: AWSCredentialsProvider, val env: Environment) {

    val s3Client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).build()

    fun createBucket(request: CreateServiceInstanceRequest): Pair<String, String> {

        var (bucketName, bucketRegion) = generateBucketNameIfNotPresent(request.parameters)

        val s3BucketRequest = CreateBucketRequest(bucketName)

        s3Client.createBucket(s3BucketRequest)

        return bucketName to bucketRegion
    }

    private fun generateBucketNameIfNotPresent(parameters: Map<String, Any>): Pair<String, String> {

        var bucketName =  if (parameters.containsKey(AwsConstants.S3_BUCKET_NAME_STRING)){
            parameters.getValue(AwsConstants.S3_BUCKET_NAME_STRING) as String
        }else{
            UUID.randomUUID().toString()
        }

        var bucketRegion = env.getProperty("AWS_DEFAULT_REGION") as String

        return bucketName to bucketRegion
    }

}