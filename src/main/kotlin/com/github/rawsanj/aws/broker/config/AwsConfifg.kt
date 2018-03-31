package com.github.rawsanj.aws.broker.config

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsConfifg {

    @Bean  // Update this method to return appropriate CredentialsProvider. See https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html
    fun awsCredentialsProvider(): EnvironmentVariableCredentialsProvider {
        return EnvironmentVariableCredentialsProvider()
    }

}