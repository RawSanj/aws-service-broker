package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.rds.AmazonRDSAsyncClientBuilder
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest
import com.amazonaws.services.rds.model.Tag
import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.DB_ALLOCATED_STORAGE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_ENGINE_STRING
import com.github.rawsanj.aws.broker.aws.repository.ServiceInstanceRepository
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.absoluteValue


@Service
class RdsOperationService(private val awsCredentialsProvider: AWSCredentialsProvider, private val env: Environment, private val serviceInstanceRepository: ServiceInstanceRepository) {

    private val LOG = LoggerFactory.getLogger(RdsOperationService::class.java)

    fun createDbInstance(request: CreateServiceInstanceRequest): Pair<CreateDBInstanceRequest, String> {

        val (dbInstanceRequest, dbInstanceRegion) = createDbInstanceRequestWithRegion(request)
        dbInstanceRequest.publiclyAccessible= true

        val instanceIdTag : Tag = Tag().withKey("InstanceId").withValue(request.serviceInstanceId)
        val regionTag : Tag = Tag().withKey("Region").withValue(dbInstanceRegion)
        dbInstanceRequest.setTags(listOf(instanceIdTag, regionTag))

        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(dbInstanceRegion).build();
        rdsAsyncClient.createDBInstanceAsync(dbInstanceRequest, RdsCreateDbInstanceAsyncHandler() )

        LOG.info("DBInstance Request Submitted for ${dbInstanceRequest.dbInstanceIdentifier} in Region: $dbInstanceRegion")

        return dbInstanceRequest to dbInstanceRegion
    }

    fun dummyCall(msg: String){
        LOG.info("I am a DUMMY CALL. Message: $msg")
    }

    fun getDbInstanceHostname(dBInstanceIdentifier: String, dbInstanceRegion: String) : String {

        LOG.info("Fetching RDS Endpoint: dBInstanceIdentifier: $dBInstanceIdentifier. dbInstanceRegion: $dbInstanceRegion")

        var endpoint : String? = null
        var noOfTries = 1

        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(dbInstanceRegion).build();
        val describeDbRequest = DescribeDBInstancesRequest().withDBInstanceIdentifier(dBInstanceIdentifier)

        while (endpoint == null){

            LOG.info("RDS Endpoint is null, Fetching RDS DBInstance Info for $noOfTries time.")

            val dBInstances = rdsAsyncClient.describeDBInstances(describeDbRequest)

            if (dBInstances.dbInstances.size > 0){

                val dbInstance = dBInstances.dbInstances[0]

                LOG.info("RDS ENDPOINT: ${dbInstance.endpoint}. Address: ${dbInstance.endpoint.address}. Port: ${dbInstance.endpoint.port}. HostedZoneId: ${dbInstance.endpoint.hostedZoneId}")

                endpoint = dbInstance.endpoint.address

            }

            Thread.sleep(10_000)
            noOfTries++
        }

        return endpoint
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