package com.github.rawsanj.aws.broker.aws.model

import javax.persistence.*


@Entity
@Table(name = "service_bindings")
data class ServiceBinding(
    @Id
    @Column(length = 50)
    val bindingId: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "parameter_name", length = 100)
    @Column(name = "parameter_value")
    @CollectionTable(name = "service_binding_parameters", joinColumns = arrayOf(JoinColumn(name = "binding_id")))
    @Convert(converter = ObjectToStringConverter::class, attributeName = "value")
    val parameters: Map<String, Any> = mutableMapOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "credential_name", length = 100)
    @Column(name = "credential_value")
    @CollectionTable(name = "service_binding_credentials", joinColumns = arrayOf(JoinColumn(name = "binding_id")))
    @Convert(converter = ObjectToStringConverter::class, attributeName = "value")
    val credentials: Map<String, Any> = mutableMapOf()

)
