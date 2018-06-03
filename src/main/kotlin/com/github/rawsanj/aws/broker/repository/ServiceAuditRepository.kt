package com.github.rawsanj.aws.broker.repository

import com.github.rawsanj.aws.broker.model.STATUS
import com.github.rawsanj.aws.broker.model.ServiceAudit
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceAuditRepository : JpaRepository<ServiceAudit, Long> {

    fun findByStatus(status: STATUS) : List<ServiceAudit>

}