package com.github.rawsanj.aws.broker

import com.github.rawsanj.aws.broker.web.model.User
import com.github.rawsanj.aws.broker.web.repository.UserRepository
import com.github.rawsanj.aws.broker.web.security.SecurityAuthorities
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class AwsServiceBrokerApplication(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder) {

    @Bean
    fun init(): ApplicationRunner {
        return ApplicationRunner {
            if (userRepository.count() === 0L) {
                userRepository.save(adminUser())
            }
        }
    }

    private fun adminUser(): User {
        return User(username = "admin",
                password = passwordEncoder.encode("supersecret"),
                authorities = listOf(SecurityAuthorities.ADMIN, SecurityAuthorities.FULL_ACCESS, SecurityAuthorities.ADMIN)
        )
    }

}

fun main(args: Array<String>) {
    runApplication<AwsServiceBrokerApplication>(*args)
}
