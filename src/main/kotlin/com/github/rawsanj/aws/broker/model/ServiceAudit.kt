package com.github.rawsanj.aws.broker.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "service_audit_events")
data class ServiceAudit(

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,

        @Column(length = 2000, name = "message")
        val message: String = "",

        @Column(length = 20, name = "event_type")
        var eventType: EventType = EventType.CREATION,

        @Column(length = 20, name = "status")
        var status: STATUS = STATUS.SUCCESS,

        @Column(length = 200, name = "occured_at")
        val eventOccuredAt: LocalDateTime = LocalDateTime.now()
)

enum class EventType {
    CREATION, DELETION
}

enum class STATUS {
    SUCCESS, FAILED
}

