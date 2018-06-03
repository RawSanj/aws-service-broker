package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DBInstance
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest
import com.github.rawsanj.aws.broker.model.EventType
import com.github.rawsanj.aws.broker.model.STATUS
import com.github.rawsanj.aws.broker.model.ServiceAudit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class RdsCreateDbInstanceAsyncHandler(private var rdsOperationService: RdsOperationService) : AsyncHandler<CreateDBInstanceRequest, DBInstance> {

    private val LOG = LoggerFactory.getLogger(RdsCreateDbInstanceAsyncHandler::class.java)

    override fun onSuccess(request: CreateDBInstanceRequest, result: DBInstance) {

        LOG.info("RDS Instance for Request { ${request.dbInstanceIdentifier}, ${request.engine}, ${request.masterUsername} } is processed Successfully")
        LOG.info("Result is : ARN - ${result.dbInstanceArn}.")

        val instanceTag = request.tags.first { it.key == "InstanceId" }
        val regionTag = request.tags.first { it.key == "Region" }

        val (dbInstanceHostname, dbPort) = rdsOperationService.getDbInstanceHostnameAndPort(request.dbInstanceIdentifier, regionTag.value)

        LOG.info("dbInstanceHostname: $dbInstanceHostname")

        rdsOperationService.updateRdsServiceInstance(instanceTag, dbInstanceHostname, dbPort, result.dbInstanceArn)

    }

    override fun onError(exception: Exception) {
        LOG.info("Error while creating RDS Instance. Message: ${exception.message}.")
        rdsOperationService.saveRdsServiceEvent(ServiceAudit(message = "AWS RDS Instance provisioning Failed. AWS Exception: ${exception.message}", eventType = EventType.CREATION, status = STATUS.FAILED))
    }

}

class RdsDeleteDbInstanceAsyncHandler(private var rdsOperationService: RdsOperationService) : AsyncHandler<DeleteDBInstanceRequest, DBInstance> {

    private val LOG = LoggerFactory.getLogger(RdsDeleteDbInstanceAsyncHandler::class.java)

    override fun onSuccess(request: DeleteDBInstanceRequest, result: DBInstance) {
        LOG.info("Deleting RDS Instance: {${request.dbInstanceIdentifier} is Successfully")
        LOG.info("Deleting RDS with ARN: ${result.dbInstanceArn}.")
        rdsOperationService.saveRdsServiceEvent(ServiceAudit(message = "AWS RDS Instance deleted successfully. AWS ARN: ${result.dbInstanceArn}", eventType = EventType.DELETION, status = STATUS.SUCCESS))
    }

    override fun onError(exception: Exception) {
        LOG.info("Error while deleting RDS Instance. Message: ${exception.message}.")
        rdsOperationService.saveRdsServiceEvent(ServiceAudit(message = "AWS RDS Instance deletion failed. AWS ARN: ${exception.message}", eventType = EventType.DELETION, status = STATUS.FAILED))
    }
}