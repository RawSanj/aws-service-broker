package com.github.rawsanj.aws.svcbroker.rds.model

import javax.persistence.*


@Entity
@Table(name = "service_bindings")
open class ServiceBinding {
    @Id
    @Column(length = 50)
    val bindingId: String?

    @ElementCollection
    @MapKeyColumn(name = "parameter_name", length = 100)
    @Column(name = "parameter_value")
    @CollectionTable(name = "service_binding_parameters", joinColumns = arrayOf(JoinColumn(name = "binding_id")))
    @Convert(converter = ObjectToStringConverter::class, attributeName = "value")
    val parameters: Map<String, Any>?

    @ElementCollection
    @MapKeyColumn(name = "credential_name", length = 100)
    @Column(name = "credential_value")
    @CollectionTable(name = "service_binding_credentials", joinColumns = arrayOf(JoinColumn(name = "binding_id")))
    @Convert(converter = ObjectToStringConverter::class, attributeName = "value")
    val credentials: Map<String, Any>?

    private constructor() {
        this.bindingId = null
        this.parameters = null
        this.credentials = null
    }

    constructor(bindingId: String, parameters: Map<String, Any>, credentials: Map<String, Any>) {
        this.bindingId = bindingId
        this.parameters = parameters
        this.credentials = credentials
    }
}
