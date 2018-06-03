package com.github.rawsanj.aws.broker

import com.github.rawsanj.aws.broker.model.User
import com.github.rawsanj.aws.broker.repository.UserRepository
import com.github.rawsanj.aws.broker.security.SecurityAuthorities
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication
class AwsServiceBrokerApplication(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder, val env: Environment) {

    private val LOG = LoggerFactory.getLogger(AwsServiceBrokerApplication::class.java)

    @Bean
    fun init(): ApplicationRunner {
        return ApplicationRunner {
            // Create Application Admin User
            if (userRepository.count() == 0L) {
                LOG.info("Creating Default Admin User for Broker App")
                userRepository.save(adminUser())
            }
        }
    }

    private fun adminUser(): User {
        val username = env.get("BROKER_APP_ADMIN_USERNAME")
        val password = env.get("BROKER_APP_ADMIN_PASSWORD")

        return User(username = username,
                password = passwordEncoder.encode(password),
                authorities = listOf(SecurityAuthorities.ADMIN, SecurityAuthorities.FULL_ACCESS, SecurityAuthorities.USER)
        )
    }

}

fun main(args: Array<String>) {
    runApplication<AwsServiceBrokerApplication>(*args)
}
