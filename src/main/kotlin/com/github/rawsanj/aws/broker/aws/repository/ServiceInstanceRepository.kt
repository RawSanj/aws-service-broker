package com.github.rawsanj.aws.broker.aws.repository

import com.github.rawsanj.aws.broker.aws.model.ServiceInstance
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceInstanceRepository : JpaRepository<ServiceInstance, String> {
}