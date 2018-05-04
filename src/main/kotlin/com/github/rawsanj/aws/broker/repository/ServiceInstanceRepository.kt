package com.github.rawsanj.aws.broker.repository

import com.github.rawsanj.aws.broker.model.ServiceInstance
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceInstanceRepository : JpaRepository<ServiceInstance, String> {
}