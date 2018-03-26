package com.github.rawsanj.aws.svcbroker.rds.service

import com.github.rawsanj.aws.svcbroker.rds.repository.ServiceBindingRepository
import com.github.rawsanj.aws.svcbroker.web.model.ApplicationInformation
import com.github.rawsanj.aws.svcbroker.web.model.User
import com.github.rawsanj.aws.svcbroker.web.security.SecurityAuthorities.FULL_ACCESS
import com.github.rawsanj.aws.svcbroker.web.security.SecurityAuthorities.RDS_ID_PREFIX
import com.github.rawsanj.aws.svcbroker.web.service.UserService
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.util.HashMap
import com.github.rawsanj.aws.svcbroker.rds.model.ServiceBinding
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException

@Service
class RDSServiceBindingService(val serviceBindingRepository: ServiceBindingRepository, val userService: UserService) : ServiceInstanceBindingService{

    private val URI_KEY = "uri"
    private val USERNAME_KEY = "username"
    private val PASSWORD_KEY = "password"

    override fun deleteServiceInstanceBinding(request: DeleteServiceInstanceBindingRequest) {

        val bindingId = request.bindingId

        if (serviceBindingRepository.existsById(bindingId)) {
            serviceBindingRepository.deleteById(bindingId)
            userService.deleteUser(bindingId)
        } else {
            throw ServiceInstanceBindingDoesNotExistException(bindingId)
        }

    }

    override fun createServiceInstanceBinding(request: CreateServiceInstanceBindingRequest): CreateServiceInstanceBindingResponse {

        val responseBuilder = CreateServiceInstanceAppBindingResponse.builder()

        val binding = serviceBindingRepository.findById(request.bindingId)

        if (binding.isPresent()) {
            responseBuilder
                    .bindingExisted(true)
                    .credentials(binding.get().credentials)
        } else {
            val user = createUser(request)

            val credentials = buildCredentials(request.serviceInstanceId, user)
            saveBinding(request, credentials)

            responseBuilder
                    .bindingExisted(false)
                    .credentials(credentials)
        }

        return responseBuilder.build()
    }

    private fun buildCredentials(instanceId: String, user: User): Map<String, String> {
        val uri = buildUri(instanceId)

        val credentials = HashMap<String, String>()
        credentials[URI_KEY] = uri
        credentials[USERNAME_KEY] = user.username as String
        credentials[PASSWORD_KEY] = user.password as String
        return credentials
    }

    private fun buildUri(instanceId: String): String {
        return UriComponentsBuilder
                .fromUriString(ApplicationInformation("").baseUrl)
                .pathSegment("bookstores", instanceId)
                .build()
                .toUriString()
    }

    private fun createUser(request: CreateServiceInstanceBindingRequest): User {
        return userService.createUser(request.bindingId,
                FULL_ACCESS, RDS_ID_PREFIX + request.serviceInstanceId)
    }

    private fun saveBinding(request: CreateServiceInstanceBindingRequest, credentials: Map<String, Any>) {
        val serviceBinding = ServiceBinding(request.bindingId, request.parameters, credentials)
        serviceBindingRepository.save(serviceBinding)
    }
}