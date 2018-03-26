package com.github.rawsanj.aws.svcbroker.web.model

import java.util.*
import javax.persistence.*


@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long?

    @Column(length = 50)
    val username: String?

    @Column(length = 100)
    val password: String?

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = arrayOf(JoinColumn(name = "user_id")))
    @Column(name = "authority")
    val authorities: List<String>?

    private constructor() {
        this.id = null
        this.username = null
        this.password = null
        this.authorities = null
    }

    constructor(username: String, password: String, vararg authorities: String) {
        this.id = null
        this.username = username
        this.password = password
        this.authorities = Arrays.asList(*authorities)
    }
}