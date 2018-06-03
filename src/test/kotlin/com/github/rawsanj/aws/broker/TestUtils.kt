package com.github.rawsanj.aws.broker

import com.github.rawsanj.aws.broker.repository.ServiceInstanceRepository
import org.awaitility.Awaitility
import org.awaitility.Duration
import java.util.concurrent.TimeUnit

data class ServiceRequest(val service_id: String, val plan_id: String, val parameters: Map<String, Any>)

object TestConstants{
    val SERVICE_ID_STRING = "service_id"
    val PLAN_ID_STRING = "plan_id"
}

fun getServiceInstanceParams(serviceInstanceRepository: ServiceInstanceRepository, serviceInstanceId: String): Map<String, Any> {
    val serviceInstance = serviceInstanceRepository.findById(serviceInstanceId).get()
    return serviceInstance.parameters
}

fun setDefaultsForAwaitility() {
    Awaitility.setDefaultPollInterval(30, TimeUnit.SECONDS);
    Awaitility.setDefaultPollDelay(Duration.ONE_MINUTE);
    Awaitility.setDefaultTimeout(Duration.ONE_MINUTE);
}