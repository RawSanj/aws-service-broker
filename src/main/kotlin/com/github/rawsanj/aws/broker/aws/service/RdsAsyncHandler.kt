package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DBInstance
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest
import org.slf4j.LoggerFactory
import java.lang.Exception


class RdsCreateDbInstanceAsyncHandler : AsyncHandler<CreateDBInstanceRequest, DBInstance> {

    private val LOG = LoggerFactory.getLogger(RdsCreateDbInstanceAsyncHandler::class.java)

    override fun onSuccess(request: CreateDBInstanceRequest, result: DBInstance) {
        LOG.info("RDS Instance for Request { ${request.dbInstanceIdentifier}, ${request.engine}, ${request.masterUsername} } is processed Successfully")
        LOG.info("Result is : ARN - ${result.dbInstanceArn}. DBName: ${result.dbName} ")
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