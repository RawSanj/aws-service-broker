package com.github.rawsanj.aws.broker.service

import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_INSTANCE_ID_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_LARGE_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_MEDIUM_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_MICRO_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_SERVICE_ID
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_NAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_BUCKET_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_SERVICE_ID
import com.github.rawsanj.aws.broker.aws.service.RdsOperationService
import com.github.rawsanj.aws.broker.aws.service.S3OperationService
import com.github.rawsanj.aws.broker.model.ServiceInstance
import com.github.rawsanj.aws.broker.repository.ServiceInstanceRepository
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse
import org.springframework.cloud.servicebroker.service.ServiceInstanceService
import org.springframework.stereotype.Service

@Service
class ServiceInstanceService(val serviceInstanceRepository: ServiceInstanceRepository, val rdsOperationService: RdsOperationService, val s3OperationService: S3OperationService) : ServiceInstanceService {

    override fun createServiceInstance(request: CreateServiceInstanceRequest): CreateServiceInstanceResponse {

        val instanceId = request.serviceInstanceId

        val responseBuilder = CreateServiceInstanceResponse.builder()

        if (serviceInstanceRepository.existsById(instanceId)) {
            responseBuilder.instanceExisted(true)
        } else {

            var requestParams : MutableMap<String, Any> = request.parameters

            when (request.serviceDefinitionId) {
                RDS_SERVICE_ID -> {
                    val (dbInfo, dbInstanceRegion) = createRdsInstance(request)
                    // Add master DB user/pass into request map to store in Service_Instance_Param if not provided by User
                    requestParams.put(RDS_DB_INSTANCE_ID_STRING, dbInfo.dbInstanceIdentifier)
                    requestParams.put(MASTER_USERNAME_STRING, dbInfo.masterUsername)
                    requestParams.put(MASTER_PASSWORD_STRING, dbInfo.masterUserPassword)
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

            when (request.serviceDefinitionId) {
                RDS_SERVICE_ID -> {
                    deleteRdsInstance(instanceId)

                }
                S3_SERVICE_ID -> {
                    deleteS3Bucket(instanceId)
                }
                else -> {
                    throw IllegalArgumentException("${request.serviceDefinitionId} is not offered! " +
                            "Available Services are <${RDS_SERVICE_ID}> and <${S3_SERVICE_ID}>")
                }
            }

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

    private fun createRdsInstance(request: CreateServiceInstanceRequest): Pair<CreateDBInstanceRequest, String> {

        val dbInfoAndRegion = when (request.planId) {

            RDS_DB_T2_MICRO_PLAN, RDS_DB_T2_MEDIUM_PLAN, RDS_DB_T2_LARGE_PLAN -> {
                rdsOperationService.createDbInstance(request)
            }
            else -> throw IllegalArgumentException("${request.planId} is not Offered under RDS Services! " +
                    "Available Plans are <${RDS_DB_T2_MICRO_PLAN}>, <${RDS_DB_T2_MEDIUM_PLAN}>, <${RDS_DB_T2_LARGE_PLAN}>")
        }

        return dbInfoAndRegion
    }

    private fun deleteRdsInstance(instanceId: String) {

        val rdsInstance = serviceInstanceRepository.findById(instanceId).get()
        val dBInstanceIdentifier = rdsInstance.parameters.get(RDS_DB_INSTANCE_ID_STRING).toString()
        val awsRegion = rdsInstance.parameters.get(AWS_REGION_STRING).toString()

        rdsOperationService.deleteDbInstance(dBInstanceIdentifier, awsRegion)
    }

    private fun createS3Bucket(request: CreateServiceInstanceRequest): Pair<String, String> {

        val awsS3BucketInfo = when (request.planId) {
            S3_BUCKET_PLAN -> {
                s3OperationService.createBucket(request)
            }
            else -> throw IllegalArgumentException("${request.planId} is not Offered under S3 Bucket Services! Available Plan is <${S3_BUCKET_PLAN}>")
        }

        return awsS3BucketInfo
    }

    private fun deleteS3Bucket(instanceId: String) {

        val s3Instance = serviceInstanceRepository.findById(instanceId).get()
        val s3BucketName = s3Instance.parameters.get(S3_BUCKET_NAME_STRING).toString()
        val awsRegion = s3Instance.parameters.get(AWS_REGION_STRING).toString()

        s3OperationService.deleteBucket(s3BucketName, awsRegion)

    }

}