package com.github.rawsanj.aws.broker.web.service

import com.github.rawsanj.aws.broker.web.model.User
import com.github.rawsanj.aws.broker.web.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service  // Create RDS User here
class UserService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun createUser(username: String, vararg authorities: String): User {
        val password = generatePassword()
        val encodedPassword = passwordEncoder.encode(password)

        // below line not required
        userRepository.save(User(username =  username, password =  encodedPassword, authorities =  listOf(*authorities)))

        return User(username =  username, password =  password, authorities = listOf(*authorities))
    }

    fun deleteUser(username: String) {
        val user = userRepository.findByUsername(username)
        if (user.isPresent) {
            userRepository.deleteById(user.get().id)
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
