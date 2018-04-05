package com.github.rawsanj.aws.broker.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.lang.reflect.Method
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
class AsyncConfig : AsyncConfigurer {

    override fun getAsyncExecutor(): Executor {
        return ConcurrentTaskExecutor(
                Executors.newFixedThreadPool(10))
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return CustomAsyncExceptionHandler()
    }

    internal class CustomAsyncExceptionHandler : AsyncUncaughtExceptionHandler {

        private val LOG = LoggerFactory.getLogger(CustomAsyncExceptionHandler::class.java)

        override fun handleUncaughtException(
                throwable: Throwable, method: Method, vararg obj: Any) {

            LOG.info("Exception message - " + throwable.message)
            LOG.info("Method name - " + method.name)
            for (param in obj) {
                LOG.info("Parameter value - $param")
            }
        }
    }
}
