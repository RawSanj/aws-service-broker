package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DBInstance
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest
import com.github.rawsanj.aws.broker.aws.model.ServiceInstance
import com.github.rawsanj.aws.broker.aws.repository.ServiceInstanceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class RdsCreateDbInstanceAsyncHandler: AsyncHandler<CreateDBInstanceRequest, DBInstance> {

    @Autowired
    private lateinit var serviceInstanceRepository: ServiceInstanceRepository

    @Autowired
    private lateinit var rdsOperationService: RdsOperationService

    private val LOG = LoggerFactory.getLogger(RdsCreateDbInstanceAsyncHandler::class.java)

    override fun onSuccess(request: CreateDBInstanceRequest, result: DBInstance) {
        LOG.info("RDS Instance for Request { ${request.dbInstanceIdentifier}, ${request.engine}, ${request.masterUsername} } is processed Successfully")
        LOG.info("Result is : ARN - ${result.dbInstanceArn}. Endpoint: ${result.endpoint} ")

        val instanceTag = request.tags.first { it.key == "InstanceId" }
        val regionTag = request.tags.first { it.key == "Region" }

        LOG.info("RDSOPS: $rdsOperationService. Is Null: ${rdsOperationService == null}")

        LOG.info("TAGs: $instanceTag $regionTag")

        rdsOperationService.dummyCall("This is Async Call.")

        val dbInstanceHostname = rdsOperationService.getDbInstanceHostname(request.dbInstanceIdentifier, regionTag.value)

        LOG.info("dbInstanceHostname: $dbInstanceHostname")

        serviceInstanceRepository.findById(instanceTag.value).ifPresent {

            val parameters : MutableMap<String, Any> = it.parameters as MutableMap
            parameters["hostname"] = dbInstanceHostname

            val serviceInstance = ServiceInstance(it.instanceId, it.serviceDefinitionId, it.planId, parameters)

           serviceInstanceRepository.save(serviceInstance)

        }
    }

    override fun onError(exception: Exception) {
        LOG.info("Error while creating RDS Instance. Message: ${exception.message}. Cause: ${exception.cause}")
    }

}

class RdsDeleteDbInstanceAsyncHandler : AsyncHandler<DeleteDBInstanceRequest, DBInstance> {

    private val LOG = LoggerFactory.getLogger(RdsDeleteDbInstanceAsyncHandler::class.java)

    override fun onSuccess(request: DeleteDBInstanceRequest, result: DBInstance) {
        LOG.info("Deleting RDS Instance: {${request.dbInstanceIdentifier} is Successfully")
        LOG.info("Deleting RDS with ARN: ${result.dbInstanceArn}.")
    }

    override fun onError(exception: Exception) {
        LOG.info("Error while deleting RDS Instance. Message: ${exception.message}. Cause: ${exception.cause}")
    }
}