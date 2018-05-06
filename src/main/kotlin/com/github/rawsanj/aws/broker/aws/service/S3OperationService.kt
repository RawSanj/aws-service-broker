package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.*
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_ACCESS_KEY_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_ARN_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_IAM_USER_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_SECRET_KEY_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_NAME_STRING
import com.github.rawsanj.aws.broker.model.ServiceBinding
import com.github.rawsanj.aws.broker.repository.ServiceInstanceRepository
import org.slf4j.LoggerFactory
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*


@Service
class S3OperationService(private val awsCredentialsProvider: AWSCredentialsProvider, private val env: Environment, private val serviceInstanceRepository: ServiceInstanceRepository) {

    private val LOG = LoggerFactory.getLogger(S3OperationService::class.java)

    fun createBucket(request: CreateServiceInstanceRequest): Pair<String, String> {

        val (bucketName, bucketRegion) = generateBucketAndRegionNameIfNotPresent(request.parameters)

        val s3BucketRequest = CreateBucketRequest(bucketName, bucketRegion)

        val s3Client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(bucketRegion).build()
        s3Client.createBucket(s3BucketRequest)

        return bucketName to bucketRegion
    }

    private fun generateBucketAndRegionNameIfNotPresent(parameters: Map<String, Any>): Pair<String, String> {

        val bucketName = if (parameters.containsKey(S3_BUCKET_NAME_STRING)) {
            parameters.getValue(S3_BUCKET_NAME_STRING) as String
        } else {
            UUID.randomUUID().toString()
        }

        val bucketRegion = if (parameters.containsKey(AWS_REGION_STRING)) {
            parameters.getValue(AWS_REGION_STRING) as String
        } else {
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

    fun createBucketSecretKeys(request: CreateServiceInstanceBindingRequest): Map<String, Any> {
        val credentials = HashMap<String, Any>()

        val serviceInstance = serviceInstanceRepository.findById(request.serviceInstanceId)

        if (serviceInstance.isPresent) {

            val parameters = serviceInstance.get().parameters
            val bucketName = parameters[S3_BUCKET_NAME_STRING]
            val awsRegion = parameters[AWS_REGION_STRING]

            credentials[S3_BUCKET_NAME_STRING] = bucketName.toString()
            credentials[AWS_REGION_STRING] = awsRegion.toString()

            //Create AmazonIdentityManagement Client
            val iamManager = AmazonIdentityManagementClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(awsRegion.toString()).build()
            // Create new IAM User for Bucket
            val iamS3BucketUser = createS3BucketUser(bucketName.toString(), iamManager)
            // Create new IAM Policy for S3 Bucket Access
            val s3PolicyARN = createS3FullAccessToSingleBucketPolicy(bucketName.toString(), iamManager)
            // Attach Policy to IAM User
            attachS3PolicyToUser(iamS3BucketUser, s3PolicyARN, iamManager)

            // Set S3 Bucket Credentials
            val s3AccessKeys = createS3AccessKeys(iamS3BucketUser, iamManager)
            credentials[AWS_ACCESS_KEY_STRING] = s3AccessKeys.accessKey.accessKeyId
            credentials[AWS_SECRET_KEY_STRING] = s3AccessKeys.accessKey.secretAccessKey
            credentials[AWS_ARN_STRING] = s3PolicyARN
            credentials[AWS_IAM_USER_STRING] = iamS3BucketUser

        } else {
            throw IllegalArgumentException("${request.serviceInstanceId} is not offered! ")
        }

        return credentials
    }

    fun deleteBucketSecretKeys(serviceBinding: ServiceBinding) {

        val s3ServiceCredentials = serviceBinding.credentials;

        LOG.debug("S3Service Credentials: $s3ServiceCredentials")

        val awsRegion = s3ServiceCredentials[AWS_REGION_STRING];
        val iamS3BucketUser = s3ServiceCredentials[AWS_IAM_USER_STRING]
        val s3PolicyARN = s3ServiceCredentials[AWS_ARN_STRING]
        val accessKeyId = s3ServiceCredentials[AWS_ACCESS_KEY_STRING]

        val iamManager = AmazonIdentityManagementClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(awsRegion.toString()).build()

        // Detach IAM Policy for S3 Bucket Access from IAM User
        val detachPolicyRequest = DetachUserPolicyRequest().withPolicyArn(s3PolicyARN.toString()).withUserName(iamS3BucketUser.toString())
        val detachUserPolicyResponse = iamManager.detachUserPolicy(detachPolicyRequest)
        LOG.debug("IAM Policy detached successfully. API Response: ${detachUserPolicyResponse.sdkResponseMetadata}")

        // Delete Policy for the S3 Bucket Access
        val deletePolicyRequest = DeletePolicyRequest().withPolicyArn(s3PolicyARN.toString());
        val deletePolicyResponse = iamManager.deletePolicy(deletePolicyRequest)
        LOG.debug("IAM Policy deleted successfully. API Response: ${deletePolicyResponse.sdkResponseMetadata}")

        // Delete IAM User's Secret Keys for Bucket Access
        val deleteAccessKeyRequest = DeleteAccessKeyRequest().withAccessKeyId(accessKeyId.toString()).withUserName(iamS3BucketUser.toString())
        val deleteAccessKeyResponse = iamManager.deleteAccessKey(deleteAccessKeyRequest)
        LOG.debug("IAM User deleted successfully. API Response: ${deleteAccessKeyResponse.sdkResponseMetadata}")

        // Delete the IAM User for S3 Access
        val deleteUserRequest = DeleteUserRequest().withUserName(iamS3BucketUser.toString())
        val deleteUserResponse = iamManager.deleteUser(deleteUserRequest)
        LOG.debug("IAM User deleted successfully. API Response: ${deleteUserResponse.sdkResponseMetadata}")
    }

    private fun createS3BucketUser(bucketName: String, iamManager: AmazonIdentityManagement): String {

        val iamS3BucketUser = "S3-USER-${bucketName}"

        val request = CreateUserRequest()
                .withUserName(iamS3BucketUser)

        val response = iamManager.createUser(request)

        LOG.info("IAM User for S3 Bucket $bucketName - $iamS3BucketUser created successfully. SDK Response: $response")

        return iamS3BucketUser

    }

    private fun createS3FullAccessToSingleBucketPolicy(bucketName: String, iamManager: AmazonIdentityManagement): String {

        val POLICY_DOCUMENT =
                "{\n" +
                        "   \"Version\": \"2012-10-17\",\n" +
                        "   \"Statement\": [\n" +
                        "     {\n" +
                        "       \"Effect\": \"Allow\",\n" +
                        "       \"Action\": [\"s3:ListBucket\"],\n" +
                        "       \"Resource\": [\"arn:aws:s3:::$bucketName\"]\n" +
                        "     },\n" +
                        "     {\n" +
                        "       \"Effect\": \"Allow\",\n" +
                        "       \"Action\": [\n" +
                        "         \"s3:PutObject\",\n" +
                        "         \"s3:GetObject\",\n" +
                        "         \"s3:DeleteObject\"\n" +
                        "       ],\n" +
                        "       \"Resource\": [\"arn:aws:s3:::$bucketName/*\"]\n" +
                        "     }\n" +
                        "   ]\n" +
                        " }"

        val s3PolicyName = "S3-POLICY-${bucketName}"

        val request = CreatePolicyRequest()
                .withPolicyName(s3PolicyName)
                .withPolicyDocument(POLICY_DOCUMENT)

        val response = iamManager.createPolicy(request)

        LOG.info("IAM Policy for S3 Bucket $bucketName - $s3PolicyName created successfully. SDK Response: ${response.policy}")

        return response.policy.arn
    }

    private fun attachS3PolicyToUser(iamS3BucketUser: String, s3PolicyARN: String, iamManager: AmazonIdentityManagement) {

        val attachUserPolicyReq = AttachUserPolicyRequest().withUserName(iamS3BucketUser).withPolicyArn(s3PolicyARN)

        val attachUserPolicyResponse = iamManager.attachUserPolicy(attachUserPolicyReq)

        LOG.debug("Policy ARN $s3PolicyARN successfully attached to $iamS3BucketUser IAM User. Response $attachUserPolicyResponse")
    }

    private fun createS3AccessKeys(iamS3BucketUser: String, iamManager: AmazonIdentityManagement): CreateAccessKeyResult {

        val request = CreateAccessKeyRequest()
                .withUserName(iamS3BucketUser)

        val accessKeyResponse = iamManager.createAccessKey(request)

        LOG.debug("S3 Access Keys created successfully for IAM User: ${accessKeyResponse.accessKey.userName}. " +
                "Access Key: ${accessKeyResponse.accessKey.accessKeyId}. Secret Key: ${accessKeyResponse.accessKey.secretAccessKey}")

        return accessKeyResponse
    }

}