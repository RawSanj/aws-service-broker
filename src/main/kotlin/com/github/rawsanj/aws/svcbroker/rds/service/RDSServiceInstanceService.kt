package com.github.rawsanj.aws.svcbroker.rds.service

import com.github.rawsanj.aws.svcbroker.rds.model.ServiceInstance
import com.github.rawsanj.aws.svcbroker.rds.repository.ServiceInstanceRepository
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse
import org.springframework.cloud.servicebroker.service.ServiceInstanceService
import org.springframework.stereotype.Service

@Service
class RDSServiceInstanceService(val serviceInstanceRepository: ServiceInstanceRepository) : ServiceInstanceService {

    override fun createServiceInstance(request: CreateServiceInstanceRequest): CreateServiceInstanceResponse {

        val instanceId = request.serviceInstanceId

        val responseBuilder = CreateServiceInstanceResponse.builder()

        if (serviceInstanceRepository.existsById(instanceId)) {
            responseBuilder.instanceExisted(true)
        } else {
            // Provision AWS RDS Instance
            saveInstance(request, instanceId)
        }
        return responseBuilder.build()
    }

    override fun deleteServiceInstance(request: DeleteServiceInstanceRequest): DeleteServiceInstanceResponse {

        val instanceId = request.serviceInstanceId

        if (serviceInstanceRepository.existsById(instanceId)) {

            //Decommision AWS RDS Instance
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
}