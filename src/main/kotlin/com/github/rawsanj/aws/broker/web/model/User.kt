package com.github.rawsanj.aws.broker.web.model

import javax.persistence.*


@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(length = 50)
    val username: String = "",

    @Column(length = 100)
    val password: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = arrayOf(JoinColumn(name = "user_id")))
    @Column(name = "authority")
    val authorities: List<String> = mutableListOf()

)