package com.github.rawsanj.aws.broker.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.github.rawsanj.aws.broker.model.ServiceBinding

interface ServiceBindingRepository : JpaRepository<ServiceBinding, String> {
}