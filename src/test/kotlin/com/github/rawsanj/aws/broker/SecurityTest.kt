package com.github.rawsanj.aws.broker

import com.github.rawsanj.aws.broker.model.User
import com.github.rawsanj.aws.broker.repository.UserRepository
import com.github.rawsanj.aws.broker.security.SecurityAuthorities
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.junit4.SpringRunner
import javax.transaction.Transactional


@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class SecurityTest {

    private val USER_LOGIN = "test-user"

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userDetailsService: UserDetailsService

    private var userOne: User? = null

    @Before
    fun init() {
            userOne = User(username = USER_LOGIN,
                password = RandomStringUtils.random(60),
                authorities = listOf(SecurityAuthorities.ADMIN, SecurityAuthorities.FULL_ACCESS, SecurityAuthorities.USER))

            userRepository.save(userOne as User)
        }

    @Test
    fun assertThatUserCanBeFoundByLogin() {

        val userDetails = userDetailsService.loadUserByUsername(USER_LOGIN)
        assertThat(userDetails).isNotNull
        assertThat(userDetails.username).isEqualTo(USER_LOGIN)
        assertThat(userDetails.authorities).isNotEmpty
        assertThat(userDetails.authorities.size).isEqualTo(3)

        assertThat(userDetails.isAccountNonExpired).isTrue()
        assertThat(userDetails.isAccountNonLocked).isTrue()
        assertThat(userDetails.isCredentialsNonExpired).isTrue()
        assertThat(userDetails.isEnabled).isTrue()

    }

}