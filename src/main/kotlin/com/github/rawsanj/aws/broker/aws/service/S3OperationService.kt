package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_NAME_STRING
import org.slf4j.LoggerFactory
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*


@Service
class S3OperationService(private val awsCredentialsProvider: AWSCredentialsProvider,private val env: Environment) {

    private val LOG = LoggerFactory.getLogger(S3OperationService::class.java)

    fun createBucket(request: CreateServiceInstanceRequest): Pair<String, String> {

        var (bucketName, bucketRegion) = generateBucketAndRegionNameIfNotPresent(request.parameters)

        val s3BucketRequest = CreateBucketRequest(bucketName, bucketRegion)

        val s3Client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(bucketRegion).build()
        s3Client.createBucket(s3BucketRequest)

        return bucketName to bucketRegion
    }

    private fun generateBucketAndRegionNameIfNotPresent(parameters: Map<String, Any>): Pair<String, String> {

        var bucketName =  if (parameters.containsKey(S3_BUCKET_NAME_STRING)){
            parameters.getValue(S3_BUCKET_NAME_STRING) as String
        }else{
            UUID.randomUUID().toString()
        }

        var bucketRegion =  if (parameters.containsKey(AWS_REGION_STRING)){
            parameters.getValue(AWS_REGION_STRING) as String
        }else{
            env.getProperty("AWS_DEFAULT_REGION") as String
        }

        return bucketName to bucketRegion
    }


    @Async
    fun deleteBucket(bucketName: String, awsRegion: String) {
        val s3Client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(awsRegion).build()

        s3Client.listObjects(bucketName).objectSummaries.forEach {
            LOG.info("Deleting Object: ${it.key}")
            s3Client.deleteObject(bucketName, it.key)
        }

        LOG.info("Deleting Bucket: $bucketName")
        s3Client.deleteBucket(bucketName)
    }

}