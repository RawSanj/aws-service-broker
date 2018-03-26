package com.github.rawsanj.aws.svcbroker

import com.github.rawsanj.aws.svcbroker.web.model.User
import com.github.rawsanj.aws.svcbroker.web.repository.UserRepository
import com.github.rawsanj.aws.svcbroker.web.security.SecurityAuthorities
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication
class AwsServiceBrokerApplication(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder){

    @Bean
    fun init() : ApplicationRunner {
        return ApplicationRunner {
            if (userRepository.count() === 0L) {
                userRepository.save(adminUser())
            }
        }
    }

    private fun adminUser(): User {
        return User("admin", passwordEncoder.encode("supersecret"),
                SecurityAuthorities.ADMIN, SecurityAuthorities.FULL_ACCESS)
    }

}

fun main(args: Array<String>) {
    runApplication<AwsServiceBrokerApplication>(*args)
}