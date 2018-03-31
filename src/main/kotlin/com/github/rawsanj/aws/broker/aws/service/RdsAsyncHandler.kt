package com.github.rawsanj.aws.broker.aws.service

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.rds.model.CreateDBInstanceRequest
import com.amazonaws.services.rds.model.DBInstance
import java.lang.Exception


class RdsAsyncHandler : AsyncHandler<CreateDBInstanceRequest, DBInstance> {

    override fun onSuccess(request: CreateDBInstanceRequest, result: DBInstance) {
        println("RDS Instance for Request { ${request.dbInstanceIdentifier}, ${request.engine}, ${request.masterUsername} } is processed Successfully")
        println("Result is : ARN - ${result.dbInstanceArn}. DBName: ${result.dbName} ")
    }

    override fun onError(exception: Exception) {
        println("Error while creating RDS Instance. Message: ${exception.message}. Cause: ${exception.cause}")
    }
}