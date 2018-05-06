package com.github.rawsanj.aws.broker

import com.github.rawsanj.aws.broker.model.User
import com.github.rawsanj.aws.broker.repository.UserRepository
import com.github.rawsanj.aws.broker.security.SecurityAuthorities
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication
class AwsServiceBrokerApplication(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder) {

    @Bean
    fun init(): ApplicationRunner {
        return ApplicationRunner {
            // Create Application Admin User
            if (userRepository.count() == 0L) {
                userRepository.save(adminUser())
            }
        }
    }

    private fun adminUser(): User {
        return User(username = "admin",
                password = passwordEncoder.encode("supersecret"),
                authorities = listOf(SecurityAuthorities.ADMIN, SecurityAuthorities.FULL_ACCESS, SecurityAuthorities.USER)
        )
    }

}

fun main(args: Array<String>) {
    runApplication<AwsServiceBrokerApplication>(*args)
}
