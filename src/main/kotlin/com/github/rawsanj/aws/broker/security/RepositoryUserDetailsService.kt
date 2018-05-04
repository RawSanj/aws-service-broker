package com.github.rawsanj.aws.broker.security

import com.github.rawsanj.aws.broker.model.User
import com.github.rawsanj.aws.broker.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class RepositoryUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
        if (!user.isPresent){
            throw UsernameNotFoundException(username)
        }

        return CustomUserDetails(user.get())
    }

    private inner class CustomUserDetails internal constructor(private val delegate: User) : UserDetails {

        override fun getUsername(): String {
            return delegate.username
        }

        override fun getPassword(): String {
            return delegate.password
        }

        override fun getAuthorities(): Collection<GrantedAuthority> {
            return delegate
                    .authorities.map { SimpleGrantedAuthority(it) }
        }

        override fun isAccountNonExpired(): Boolean {
            return true
        }

        override fun isAccountNonLocked(): Boolean {
            return true
        }

        override fun isCredentialsNonExpired(): Boolean {
            return true
        }

        override fun isEnabled(): Boolean {
            return true
        }

        override fun toString(): String {
            return "CustomUserDetails{" +
                    "username=" + username +
                    ", password=" + password +
                    ", authorities=" + authorities +
                    '}'.toString()
        }
    }
}