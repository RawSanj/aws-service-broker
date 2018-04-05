package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.rds.AmazonRDSAsyncClientBuilder
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest
import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.DB_ALLOCATED_STORAGE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_ENGINE_STRING
import com.github.rawsanj.aws.broker.aws.repository.ServiceInstanceRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.absoluteValue


@Service
class RdsOperationService(private val awsCredentialsProvider: AWSCredentialsProvider, private val env: Environment, private val serviceInstanceRepository: ServiceInstanceRepository) {

    fun createDbInstance(request: CreateServiceInstanceRequest): Pair<CreateDBInstanceRequest, String> {

        val (dbInstanceRequest, dbInstanceRegion) = createDbInstanceRequestWithRegion(request)
        dbInstanceRequest.publiclyAccessible= true

        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(dbInstanceRegion).build();
        rdsAsyncClient.createDBInstanceAsync(dbInstanceRequest, RdsCreateDbInstanceAsyncHandler() )

        return dbInstanceRequest to dbInstanceRegion
    }

    private fun createDbInstanceRequestWithRegion(request: CreateServiceInstanceRequest): Pair<CreateDBInstanceRequest, String> {

        val parameters = request.parameters

        val allocatedStorage = if (parameters.containsKey(DB_ALLOCATED_STORAGE_STRING)) {
            parameters.getValue(DB_ALLOCATED_STORAGE_STRING).toString().toInt()
        } else {
            20
        }

        val engine = if (parameters.containsKey(RDS_ENGINE_STRING)) {
            parameters.getValue(RDS_ENGINE_STRING) as String
        } else {
            "mysql"
        }

        val masterUsername = if (parameters.containsKey(MASTER_USERNAME_STRING)) {
            parameters.getValue(MASTER_USERNAME_STRING) as String
        } else {
            "RdsAdmin" +  Random().nextInt(10000).absoluteValue
        }

        val masterUserPassword = if (parameters.containsKey(MASTER_PASSWORD_STRING)) {
            parameters.getValue(MASTER_PASSWORD_STRING) as String
        } else {
            UUID.randomUUID().toString()
        }

        val rdsInstanceRegion =  if (parameters.containsKey(AwsConstants.AWS_REGION_STRING)){
            parameters.getValue(AwsConstants.AWS_REGION_STRING) as String
        }else{
            env.getProperty("AWS_DEFAULT_REGION") as String
        }

        val dBInstanceIdentifier = RandomStringUtils.randomAlphabetic(10).toLowerCase()

        return CreateDBInstanceRequest(dBInstanceIdentifier, allocatedStorage, request.planId, engine, masterUsername, masterUserPassword) to rdsInstanceRegion
    }

    @Async
    fun deleteDbInstance(dBInstanceIdentifier: String, awsRegion: String) {

        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(awsRegion).build();
        val dBInstanceRequest = DeleteDBInstanceRequest(dBInstanceIdentifier).withSkipFinalSnapshot(true) // Skipping Final SnapShot

        rdsAsyncClient.deleteDBInstanceAsync(dBInstanceRequest, RdsDeleteDbInstanceAsyncHandler())

    }

//    fun createRdsUser(request: CreateServiceInstanceBindingRequest) : Map<String, Any> {
//
//        val rdsInstance = serviceInstanceRepository.findById(request.serviceInstanceId).get()
//        val dbInstanceRegion = rdsInstance.parameters.get(AWS_REGION_STRING).toString()
//        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(dbInstanceRegion).build();
//
//        return emptyMap()
//
//    }

}