package com.github.rawsanj.aws.svcbroker.rds.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.github.rawsanj.aws.svcbroker.rds.model.ServiceBinding

interface ServiceBindingRepository : JpaRepository<ServiceBinding, String> {
}