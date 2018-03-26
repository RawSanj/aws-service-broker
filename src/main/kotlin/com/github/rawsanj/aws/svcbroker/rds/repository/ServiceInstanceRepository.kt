package com.github.rawsanj.aws.svcbroker.rds.repository

import com.github.rawsanj.aws.svcbroker.rds.model.ServiceInstance
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceInstanceRepository : JpaRepository<ServiceInstance, String> {
}