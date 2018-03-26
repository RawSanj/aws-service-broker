package com.github.rawsanj.aws.svcbroker.web.service

import com.github.rawsanj.aws.svcbroker.web.model.User
import com.github.rawsanj.aws.svcbroker.web.repository.UserRepository
import com.github.rawsanj.aws.svcbroker.web.security.SecurityAuthorities
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class UserService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun createUser(username: String, vararg authorities: String): User {
        val password = generatePassword()
        val encodedPassword = passwordEncoder.encode(password)

        userRepository.save(User(username, encodedPassword, *authorities))

        return User(username, password, *authorities)
    }

    fun deleteUser(username: String) {
        val user = userRepository.findByUsername(username)
        if (user.isPresent) {
            userRepository.deleteById(user.get().id as Long)
        }
    }

    private fun generatePassword(): String {
        val sb = StringBuilder(PASSWORD_LENGTH)
        for (i in 0 until PASSWORD_LENGTH) {
            sb.append(PASSWORD_CHARS[RANDOM.nextInt(PASSWORD_CHARS.length)])
        }
        return sb.toString()
    }

    companion object {
        private val PASSWORD_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        private val PASSWORD_LENGTH = 12

        private val RANDOM = SecureRandom()
    }
}
