package com.github.rawsanj.aws.broker.aws.service

import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_LARGE_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_MEDIUM_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_MICRO_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_SERVICE_ID
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_NAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_SERVICE_ID
import com.github.rawsanj.aws.broker.aws.model.ServiceInstance
import com.github.rawsanj.aws.broker.aws.repository.ServiceInstanceRepository
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse
import org.springframework.cloud.servicebroker.service.ServiceInstanceService
import org.springframework.stereotype.Service

@Service
class AwsServiceInstanceService(val serviceInstanceRepository: ServiceInstanceRepository, val rdsService: RdsOperationService, val s3Service: S3OperationService) : ServiceInstanceService {

    override fun createServiceInstance(request: CreateServiceInstanceRequest): CreateServiceInstanceResponse {

        val instanceId = request.serviceInstanceId

        val responseBuilder = CreateServiceInstanceResponse.builder()

        if (serviceInstanceRepository.existsById(instanceId)) {
            responseBuilder.instanceExisted(true)
        } else {

            var requestParams: MutableMap<String, Any> = request.parameters

            when (request.serviceDefinitionId) {
                RDS_SERVICE_ID -> {
                    val (masterUserName, masterPassword, dbInstanceRegion) = createRdsInstance(request)
                    // Add master DB user/pass into request map to store in Service_Instance_Param if not provided by User
                    requestParams.put(MASTER_USERNAME_STRING, masterUserName)
                    requestParams.put(MASTER_PASSWORD_STRING, masterPassword)
                    requestParams.put(AWS_REGION_STRING, dbInstanceRegion)
                }
                S3_SERVICE_ID -> {
                    val awsBucketInfo = createS3Bucket(request)
                    // Add S3 Bucket Name and Region into request map to store in Service_Instance_Param if not provided by User
                    requestParams.put(S3_BUCKET_NAME_STRING, awsBucketInfo.first)
                    requestParams.put(AWS_REGION_STRING, awsBucketInfo.second)
                }
                else -> {
                    throw IllegalArgumentException("${request.serviceDefinitionId} is not offered! " +
                            "Available Services are <${RDS_SERVICE_ID}> and <${S3_SERVICE_ID}>")
                }
            }

            saveInstance(request, instanceId)
        }
        return responseBuilder
                .build()
    }

    override fun deleteServiceInstance(request: DeleteServiceInstanceRequest): DeleteServiceInstanceResponse {

        val instanceId = request.serviceInstanceId

        if (serviceInstanceRepository.existsById(instanceId)) {

            //Decommision AWS RDS Instance
            serviceInstanceRepository.deleteById(instanceId)

            return DeleteServiceInstanceResponse.builder().build()
        } else {
            throw ServiceInstanceDoesNotExistException(instanceId)
        }
    }

    private fun saveInstance(request: CreateServiceInstanceRequest, instanceId: String) {
        val serviceInstance = ServiceInstance(instanceId, request.serviceDefinitionId,
                request.planId, request.parameters)
        serviceInstanceRepository.save(serviceInstance)
    }

    private fun createRdsInstance(request: CreateServiceInstanceRequest): Triple<String, String, String> {

        val dbUserNamePasswordAndRegion = when (request.planId) {

            RDS_DB_T2_MICRO_PLAN, RDS_DB_T2_MEDIUM_PLAN, RDS_DB_T2_LARGE_PLAN -> {
                rdsService.createDbInstance(request)
            }
            else -> throw IllegalArgumentException("${request.planId} is not Offered under RDS Services! " +
                    "Available Plans are <${RDS_DB_T2_MICRO_PLAN}>, <${RDS_DB_T2_MEDIUM_PLAN}>, <${RDS_DB_T2_LARGE_PLAN}>")
        }

        return dbUserNamePasswordAndRegion
    }

    private fun createS3Bucket(request: CreateServiceInstanceRequest): Pair<String, String> {

        val awsS3BucketInfo = when (request.planId) {
            S3_BUCKET_PLAN -> {
                s3Service.createBucket(request)
            }
            else -> throw IllegalArgumentException("${request.planId} is not Offered under S3 Bucket Services! Available Plan is <${S3_BUCKET_PLAN}>")
        }

        return awsS3BucketInfo
    }
}