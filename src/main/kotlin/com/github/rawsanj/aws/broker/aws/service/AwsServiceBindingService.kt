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
import java.util.*
import kotlin.collections.HashMap

@Service
class AwsServiceBindingService(val serviceBindingRepository: ServiceBindingRepository,
                               val rdsOperationService: RdsOperationService,
                               val s3OperationService: S3OperationService) : ServiceInstanceBindingService {

    private val URI_KEY = "uri"
    private val USERNAME_KEY = "username"
    private val PASSWORD_KEY = "password"

    override fun createServiceInstanceBinding(request: CreateServiceInstanceBindingRequest): CreateServiceInstanceBindingResponse {

        val responseBuilder = CreateServiceInstanceAppBindingResponse.builder()

        val binding = serviceBindingRepository.findById(request.bindingId)

        if (binding.isPresent) {
            responseBuilder
                    .bindingExisted(true)
                    .credentials(binding.get().credentials)
        } else {

            // Create RDS User Here

            var credentials = emptyMap<String, Any>()

            when (request.serviceDefinitionId) {
                AwsConstants.RDS_SERVICE_ID -> {
                    // Create RDS User
//                    credentials = rdsOperationService.createRdsUser(request)
                }
                AwsConstants.S3_SERVICE_ID -> {
                    // Create S3
                }
                else -> {
                    throw IllegalArgumentException("${request.serviceDefinitionId} is not offered! " +
                            "Available Services are <${AwsConstants.RDS_SERVICE_ID}> and <${AwsConstants.S3_SERVICE_ID}>")
                }
            }


            val user = createUser(request)

            // val credentials = buildCredentials(request.serviceInstanceId, user)
            saveBinding(request, credentials)

            responseBuilder
                    .bindingExisted(false)
                    .credentials(credentials)
        }

        return responseBuilder.build()
    }

    override fun deleteServiceInstanceBinding(request: DeleteServiceInstanceBindingRequest) {

        val bindingId = request.bindingId

        if (serviceBindingRepository.existsById(bindingId)) {
            serviceBindingRepository.deleteById(bindingId)
//            userService.deleteUser(bindingId)
            // Delete RDS User Here
        } else {
            throw ServiceInstanceBindingDoesNotExistException(bindingId)
        }

    }

    private fun buildCredentials(instanceId: String, user: User): Map<String, String> {
        val uri = getRdsInstanceUri(instanceId)

        val credentials = HashMap<String, String>()
        credentials[URI_KEY] = uri  // DB URI
        credentials[USERNAME_KEY] = user.username
        credentials[PASSWORD_KEY] = user.password
        return credentials
    }

    // Fetch RDS Instance URI
    private fun getRdsInstanceUri(instanceId: String): String {
        return UriComponentsBuilder
                .fromUriString(ApplicationInformation("").baseUrl)
                .pathSegment("/", instanceId)
                .build()
                .toUriString()
    }

    private fun createUser(request: CreateServiceInstanceBindingRequest): User {
//        return userService.createUser(request.bindingId,
//                FULL_ACCESS, RDS_ID_PREFIX + request.serviceInstanceId)
        return User()
    }

    private fun saveBinding(request: CreateServiceInstanceBindingRequest, credentials: Map<String, Any>) {
        val serviceBinding = ServiceBinding(request.bindingId, request.parameters, credentials)
        serviceBindingRepository.save(serviceBinding)
    }
}