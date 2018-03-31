package com.github.rawsanj.aws.broker.aws.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.github.rawsanj.aws.broker.aws.model.ServiceBinding

interface ServiceBindingRepository : JpaRepository<ServiceBinding, String> {
}