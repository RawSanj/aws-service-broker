package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.rds.AmazonRDSAsyncClientBuilder
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DBInstance
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.DB_ALLOCATED_STORAGE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_ENGINE
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*
import kotlin.math.absoluteValue


@Service
class RdsService(val awsCredentialsProvider: AWSCredentialsProvider) {

    val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).build();

    fun createDbInstance(request: CreateServiceInstanceRequest): Pair<String, String> {

        val dbInstanceRequest = createDbInstanceRequest(request)

        val masterUsername = dbInstanceRequest.masterUsername
        val masterUserPassword = dbInstanceRequest.masterUserPassword

        rdsAsyncClient.createDBInstanceAsync(dbInstanceRequest, RdsAsyncHandler() )

        return masterUsername to masterUserPassword
    }

    fun createDbInstanceRequest(request: CreateServiceInstanceRequest): CreateDBInstanceRequest {

        var parameters = request.parameters

        var allocatedStorage = if (parameters.containsKey(DB_ALLOCATED_STORAGE_STRING)) {
            parameters.getValue(DB_ALLOCATED_STORAGE_STRING).toString().toInt()
        } else {
            20
        }

        var engine = if (parameters.containsKey(RDS_ENGINE)) {
            parameters.getValue(RDS_ENGINE) as String
        } else {
            "mysql"
        }

        var masterUsername = if (parameters.containsKey(MASTER_USERNAME_STRING)) {
            parameters.getValue(MASTER_USERNAME_STRING) as String
        } else {
            "RdsAdmin" +  Random().nextInt().absoluteValue
        }

        var masterUserPassword = if (parameters.containsKey(MASTER_PASSWORD_STRING)) {
            parameters.getValue(MASTER_PASSWORD_STRING) as String
        } else {
            UUID.randomUUID().toString()
        }

        return CreateDBInstanceRequest(request.serviceInstanceId, allocatedStorage, request.planId, engine, masterUsername, masterUserPassword)
    }

}