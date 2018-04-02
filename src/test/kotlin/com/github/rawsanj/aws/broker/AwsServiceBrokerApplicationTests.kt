package com.github.rawsanj.aws.broker

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import kotlin.math.absoluteValue

//@RunWith(SpringRunner::class)
//@SpringBootTest
class AwsServiceBrokerApplicationTests {

	@Test
	fun contextLoads() {


//		var requestParams : MutableMap<String, Any> = mutableMapOf("user".to("sanjay"), "password".to("yolo"))
//
//		println(requestParams)
//
//		requestParams.put("user", "notme")
//		requestParams.put("password", "nahh")
//		requestParams.put("msg","hello")
//
//		println(requestParams)
//
//
//        println(UUID.randomUUID().toString())

        println(Random().nextInt(1000).absoluteValue)

        val no = -100
        println(no.absoluteValue)

	}

}
