package com.github.rawsanj.aws.broker.model

import javax.persistence.*

@Entity
@Table(name = "service_instances")
data class ServiceInstance(

    @Id
    @Column(length = 50)
    val instanceId: String = "",

    @Column(length = 50)
    val serviceDefinitionId: String = "",

    @Column(length = 50)
    val planId: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "parameter_name", length = 100)
    @Column(name = "parameter_value")
    @CollectionTable(name = "service_instance_parameters", joinColumns = arrayOf(JoinColumn(name = "instance_id")))
    @Convert(converter = ObjectToStringConverter::class, attributeName = "value")
    val parameters: Map<String, Any> = mutableMapOf()

)