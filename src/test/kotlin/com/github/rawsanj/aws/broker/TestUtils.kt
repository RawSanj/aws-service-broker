package com.github.rawsanj.aws.broker

data class ServiceRequest(val service_id: String, val plan_id: String, val parameters: Map<String, Any>)

object TestConstants{
    val SERVICE_ID_STRING = "service_id"
    val PLAN_ID_STRING = "plan_id"
}