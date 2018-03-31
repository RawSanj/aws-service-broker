package com.github.rawsanj.aws.broker.config

import com.github.rawsanj.aws.broker.web.security.SecurityAuthorities
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


@Configuration
@EnableWebSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
            http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/v2/**").hasAuthority(SecurityAuthorities.ADMIN)
                .requestMatchers(EndpointRequest.to("info", "health")).permitAll()
                .antMatchers("/h2-console/**/**").permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(SecurityAuthorities.ADMIN)
                .and()
                .httpBasic()

            http
                .headers()
                .frameOptions().sameOrigin()  // required to set for H2 else H2 Console will be blank.
                .cacheControl();
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}