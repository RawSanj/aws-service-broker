package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rds.AmazonRDSAsyncClientBuilder
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.DB_ALLOCATED_STORAGE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_ENGINE_STRING
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.absoluteValue


@Service
class RdsOperationService(val awsCredentialsProvider: AWSCredentialsProvider, val env: Environment) {

    fun createDbInstance(request: CreateServiceInstanceRequest): Triple<String, String, String> {

        val (dbInstanceRequest, dbInstanceRegion) = createDbInstanceRequestWithRegion(request)
        dbInstanceRequest.publiclyAccessible= true

        val masterUsername = dbInstanceRequest.masterUsername
        val masterUserPassword = dbInstanceRequest.masterUserPassword

        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(dbInstanceRegion).build();
        rdsAsyncClient.createDBInstanceAsync(dbInstanceRequest, RdsAsyncHandler() )

        return Triple(masterUsername, masterUserPassword, dbInstanceRegion)
    }

    fun createDbInstanceRequestWithRegion(request: CreateServiceInstanceRequest): Pair<CreateDBInstanceRequest, String> {

        var parameters = request.parameters

        var allocatedStorage = if (parameters.containsKey(DB_ALLOCATED_STORAGE_STRING)) {
            parameters.getValue(DB_ALLOCATED_STORAGE_STRING).toString().toInt()
        } else {
            20
        }

        var engine = if (parameters.containsKey(RDS_ENGINE_STRING)) {
            parameters.getValue(RDS_ENGINE_STRING) as String
        } else {
            "mysql"
        }

        var masterUsername = if (parameters.containsKey(MASTER_USERNAME_STRING)) {
            parameters.getValue(MASTER_USERNAME_STRING) as String
        } else {
            "RdsAdmin" +  Random().nextInt(10000).absoluteValue
        }

        var masterUserPassword = if (parameters.containsKey(MASTER_PASSWORD_STRING)) {
            parameters.getValue(MASTER_PASSWORD_STRING) as String
        } else {
            UUID.randomUUID().toString()
        }

        var rdsInstanceRegion =  if (parameters.containsKey(AwsConstants.AWS_REGION_STRING)){
            parameters.getValue(AwsConstants.AWS_REGION_STRING) as String
        }else{
            env.getProperty("AWS_DEFAULT_REGION") as String
        }

        return CreateDBInstanceRequest(request.serviceInstanceId, allocatedStorage, request.planId, engine, masterUsername, masterUserPassword) to rdsInstanceRegion
    }

}