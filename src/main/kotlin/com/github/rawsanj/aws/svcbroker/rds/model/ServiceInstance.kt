package com.github.rawsanj.aws.svcbroker.rds.model

import javax.persistence.*

@Entity
@Table(name = "service_instances")
open class ServiceInstance {
    @Id
    @Column(length = 50)
    val instanceId: String?

    @Column(length = 50)
    val serviceDefinitionId: String?

    @Column(length = 50)
    val planId: String?

    @ElementCollection
    @MapKeyColumn(name = "parameter_name", length = 100)
    @Column(name = "parameter_value")
    @CollectionTable(name = "service_instance_parameters", joinColumns = arrayOf(JoinColumn(name = "instance_id")))
    @Convert(converter = ObjectToStringConverter::class, attributeName = "value")
    val parameters: Map<String, Any>?

    private constructor() {
        instanceId = null
        serviceDefinitionId = null
        planId = null
        parameters = null
    }

    constructor(instanceId: String, serviceDefinitionId: String, planId: String,
                parameters: Map<String, Any>) {
        this.instanceId = instanceId
        this.serviceDefinitionId = serviceDefinitionId
        this.planId = planId
        this.parameters = parameters
    }
}
