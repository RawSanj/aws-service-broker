package com.github.rawsanj.aws.broker.web.repository

import com.github.rawsanj.aws.broker.web.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
}