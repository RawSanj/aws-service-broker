package com.github.rawsanj.aws.broker.aws.service

import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_NAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
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
class AwsServiceInstanceService(val serviceInstanceRepository: ServiceInstanceRepository, val rdsService: RdsService, val s3Service: S3Service) : ServiceInstanceService {

    override fun createServiceInstance(request: CreateServiceInstanceRequest): CreateServiceInstanceResponse {

        val instanceId = request.serviceInstanceId

        val responseBuilder = CreateServiceInstanceResponse.builder()

        if (serviceInstanceRepository.existsById(instanceId)) {
            responseBuilder.instanceExisted(true)
        } else {

            var requestParams: MutableMap<String, Any> = request.parameters

            when (request.serviceDefinitionId) {
                AwsConstants.RDS_SERVICE_ID -> {
                    val dbMasterUsernamePassword = createRdsInstance(request)
                    // Add master DB user/pass into request map to store in Service_Instance_Param if not provided by User
                    requestParams.put(MASTER_USERNAME_STRING, dbMasterUsernamePassword.first)
                    requestParams.put(MASTER_PASSWORD_STRING, dbMasterUsernamePassword.second)
                }
                AwsConstants.S3_SERVICE_ID -> {
                    val awsBucketInfo = createS3Bucket(request)
                    // Add S3 Bucket Name and Region into request map to store in Service_Instance_Param if not provided by User
                    requestParams.put(S3_BUCKET_NAME_STRING, awsBucketInfo.first)
                    requestParams.put(S3_BUCKET_REGION_STRING, awsBucketInfo.second)
                }
                else -> {
                    throw IllegalArgumentException("${request.serviceDefinitionId} is not offered! " +
                            "Available Services are <${AwsConstants.RDS_SERVICE_ID}> and <${AwsConstants.S3_SERVICE_ID}>")
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

    private fun createRdsInstance(request: CreateServiceInstanceRequest): Pair<String, String> {

        val dbUserNameAndPassword = when (request.planId) {

            AwsConstants.RDS_DB_T2_MICRO_PLAN, AwsConstants.RDS_DB_T2_MEDIUM_PLAN, AwsConstants.RDS_DB_T2_LARGE_PLAN -> {
                rdsService.createDbInstance(request)
            }
            else -> throw IllegalArgumentException("${request.planId} is not Offered under RDS Services! " +
                    "Available Plans are <${AwsConstants.RDS_DB_T2_MICRO_PLAN}>, <${AwsConstants.RDS_DB_T2_MEDIUM_PLAN}>, <${AwsConstants.RDS_DB_T2_LARGE_PLAN}>")
        }

        return dbUserNameAndPassword
    }

    private fun createS3Bucket(request: CreateServiceInstanceRequest): Pair<String, String> {

        val awsS3BucketInfo = when (request.planId) {
            AwsConstants.S3_BUCKET_PLAN -> {
                s3Service.createBucket(request)
            }
            else -> throw IllegalArgumentException("${request.planId} is not Offered under S3 Bucket Services! Available Plan is <${AwsConstants.S3_BUCKET_PLAN}>")
        }

        return awsS3BucketInfo
    }
}