package com.github.rawsanj.aws.broker.aws.service

import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.model.ServiceBinding
import com.github.rawsanj.aws.broker.aws.repository.ServiceBindingRepository
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService
import org.springframework.stereotype.Service

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

        val binding = serviceBindingRepository.findById(request.bindingId);

        if (!binding.isPresent) {
            throw ServiceInstanceBindingDoesNotExistException(request.bindingId)
        } else {

            when (request.serviceDefinitionId) {

                AwsConstants.RDS_SERVICE_ID -> {
                    // Delete RDS Credentials from Service Bindings
                    serviceBindingRepository.deleteById(request.bindingId)
                }
                AwsConstants.S3_SERVICE_ID -> {
                    // Delete S3 Secret Keys, IAM User and Policy
                    s3OperationService.deleteBucketSecretKeys(binding.get())
                    serviceBindingRepository.deleteById(request.bindingId)
                }
                else -> {
                    throw IllegalArgumentException("Unsupported Service Definition - ${request.serviceDefinition}")
                }
            }

        }

    }

    private fun saveBinding(request: CreateServiceInstanceBindingRequest, credentials: Map<String, Any>) {
        val serviceBinding = ServiceBinding(request.bindingId, request.parameters, credentials)
        serviceBindingRepository.save(serviceBinding)
    }

}