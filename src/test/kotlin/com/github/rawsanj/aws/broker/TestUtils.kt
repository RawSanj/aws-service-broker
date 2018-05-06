package com.github.rawsanj.aws.broker

data class ServiceRequest(val service_id: String, val plan_id: String, val parameters: Map<String, Any>)