package com.github.rawsanj.aws.broker.aws.service

import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.model.ServiceBinding
import com.github.rawsanj.aws.broker.aws.repository.ServiceBindingRepository
import com.github.rawsanj.aws.broker.web.model.ApplicationInformation
import com.github.rawsanj.aws.broker.web.model.User
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import kotlin.collections.HashMap

@Service
class AwsServiceBindingService(val serviceBindingRepository: ServiceBindingRepository,
                               val rdsOperationService: RdsOperationService,
                               val s3OperationService: S3OperationService) : ServiceInstanceBindingService {

    override fun createServiceInstanceBinding(request: CreateServiceInstanceBindingRequest): CreateServiceInstanceBindingResponse {

        val responseBuilder = CreateServiceInstanceAppBindingResponse.builder()

        val binding = serviceBindingRepository.findById(request.bindingId)

        if (binding.isPresent) {
            responseBuilder
                    .bindingExisted(true)
                    .credentials(binding.get().credentials)
        } else {

            val credentials = when (request.serviceDefinitionId) {

                AwsConstants.RDS_SERVICE_ID -> {
                    // Return RDS Credentials
                    rdsOperationService.fetchRdsCredentials(request)
                }
                AwsConstants.S3_SERVICE_ID -> {
                    // Create S3 Keys
                    s3OperationService.createBucketSecretKeys(request)
                }
                else -> {
                    throw IllegalArgumentException("${request.serviceDefinitionId} is not offered! " +
                            "Available Services are <${AwsConstants.RDS_SERVICE_ID}> and <${AwsConstants.S3_SERVICE_ID}>")
                }
            }

            saveBinding(request, credentials)

            responseBuilder
                    .bindingExisted(false)
                    .credentials(credentials)
        }

        return responseBuilder.build()
    }

    override fun deleteServiceInstanceBinding(request: DeleteServiceInstanceBindingRequest) {

        val bindingId = request.bindingId

        TODO("Delete RDS or S3 Creds is not Implemented")

        deleteBinding(bindingId)

    }

    private fun saveBinding(request: CreateServiceInstanceBindingRequest, credentials: Map<String, Any>) {
        val serviceBinding = ServiceBinding(request.bindingId, request.parameters, credentials)
        serviceBindingRepository.save(serviceBinding)
    }

    private fun deleteBinding(bindingId: String) {

        if (serviceBindingRepository.existsById(bindingId)) {
            serviceBindingRepository.deleteById(bindingId)
        } else {
            throw ServiceInstanceBindingDoesNotExistException(bindingId)
        }
    }
}