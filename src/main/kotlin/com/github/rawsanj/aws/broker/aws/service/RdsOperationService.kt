package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.rds.AmazonRDSAsyncClientBuilder
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest
import com.amazonaws.services.rds.model.Tag
import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_ARN_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.DB_ALLOCATED_STORAGE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.HOSTNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.PORT_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_ENGINE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.model.ServiceInstance
import com.github.rawsanj.aws.broker.aws.repository.ServiceInstanceRepository
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.absoluteValue


@Service
class RdsOperationService(private val awsCredentialsProvider: AWSCredentialsProvider, private val env: Environment, private val serviceInstanceRepository: ServiceInstanceRepository) {

    private val LOG = LoggerFactory.getLogger(RdsOperationService::class.java)

    fun createDbInstance(request: CreateServiceInstanceRequest): Pair<CreateDBInstanceRequest, String> {

        val (dbInstanceRequest, dbInstanceRegion) = createDbInstanceRequestWithRegion(request)
        dbInstanceRequest.publiclyAccessible = true

        val instanceIdTag: Tag = Tag().withKey("InstanceId").withValue(request.serviceInstanceId)
        val regionTag: Tag = Tag().withKey("Region").withValue(dbInstanceRegion)
        dbInstanceRequest.setTags(listOf(instanceIdTag, regionTag))

        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(dbInstanceRegion).build();
        rdsAsyncClient.createDBInstanceAsync(dbInstanceRequest, RdsCreateDbInstanceAsyncHandler(this))

        LOG.info("DBInstance Request Submitted for ${dbInstanceRequest.dbInstanceIdentifier} in Region: $dbInstanceRegion")

        return dbInstanceRequest to dbInstanceRegion
    }

    fun getDbInstanceHostnameAndPort(dBInstanceIdentifier: String, dbInstanceRegion: String): Pair<String, Int> {

        LOG.info("Fetching RDS Endpoint: dBInstanceIdentifier: $dBInstanceIdentifier. dbInstanceRegion: $dbInstanceRegion")

        var endpoint: String? = null
        var port = 0;
        var noOfTries = 1
        var retrySeconds: Long = 60_000 * 3

        val rdsAsyncClient = AmazonRDSAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(dbInstanceRegion).build();
        val describeDbRequest = DescribeDBInstancesRequest().withDBInstanceIdentifier(dBInstanceIdentifier)

        while (endpoint == null) {

            LOG.info("Fetching RDS DBInstance Info for $noOfTries time now.")

            val dBInstances = rdsAsyncClient.describeDBInstances(describeDbRequest)

            if (dBInstances.dbInstances.size > 0) {

                val dbInstance = dBInstances.dbInstances[0]
                if (dbInstance.endpoint != null) {

                    LOG.info("RDS ENDPOINT: ${dbInstance.endpoint}. Address: ${dbInstance.endpoint.address}. Port: ${dbInstance.endpoint.port}. HostedZoneId: ${dbInstance.endpoint.hostedZoneId}")
                    endpoint = dbInstance.endpoint.address
                    port = dbInstance.endpoint.port
                } else {

                    LOG.info("RDS Instance is not yet Ready yet. Retrying again  in $retrySeconds Seconds!")

                    Thread.sleep(retrySeconds)

                    noOfTries++
                    retrySeconds = retrySeconds / 2
                }
            }
        }

        return endpoint to port
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
            "RdsAdmin" + Random().nextInt(10000).absoluteValue
        }

        val masterUserPassword = if (parameters.containsKey(MASTER_PASSWORD_STRING)) {
            parameters.getValue(MASTER_PASSWORD_STRING) as String
        } else {
            UUID.randomUUID().toString()
        }

        val rdsInstanceRegion = if (parameters.containsKey(AWS_REGION_STRING)) {
            parameters.getValue(AWS_REGION_STRING) as String
        } else {
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

    fun updateRdsServiceInstance(instanceTag: Tag, dbInstanceHostname: String, dbPort: Int, dbInstanceArn: String ){

        serviceInstanceRepository.findById(instanceTag.value).ifPresent {

            LOG.info("ServiceInstance Id: ${it.instanceId} is present, updating Hostname and port")

            val parameters: MutableMap<String, Any> = it.parameters as MutableMap
            parameters[HOSTNAME_STRING] = dbInstanceHostname
            parameters[PORT_STRING] = dbPort
            parameters[AWS_ARN_STRING] = dbInstanceArn

            val serviceInstance = ServiceInstance(it.instanceId, it.serviceDefinitionId, it.planId, parameters)
            serviceInstanceRepository.save(serviceInstance)
        }

    }

    fun fetchRdsCredentials(request: CreateServiceInstanceBindingRequest): Map<String, Any> {

        val credentials = HashMap<String, Any>()

        val serviceInstance = serviceInstanceRepository.findById(request.serviceInstanceId)

        if (serviceInstance.isPresent) {

            val parameters = serviceInstance.get().parameters
            credentials[HOSTNAME_STRING] = parameters[HOSTNAME_STRING] as String
            credentials[PORT_STRING] = parameters[PORT_STRING] as String
            credentials[USERNAME_STRING] = parameters[MASTER_USERNAME_STRING] as String
            credentials[PASSWORD_STRING] = parameters[MASTER_PASSWORD_STRING] as String
        } else {
            throw IllegalArgumentException("${request.serviceInstanceId} is not offered! ")
        }

        return credentials
    }

}