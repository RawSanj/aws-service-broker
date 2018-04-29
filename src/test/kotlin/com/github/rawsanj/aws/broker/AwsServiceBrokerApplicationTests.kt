package com.github.rawsanj.aws.broker

import com.amazonaws.services.rds.model.Tag
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

//        println(Random().nextInt(1000).absoluteValue)
//
//        val no = -100
//        println(no.absoluteValue)
//
//        var tags = listOf<Tag>(Tag().withKey("InstanceId").withValue("ufgjygfjgjfgsejfjsgfe"), Tag().withKey("Region").withValue("us-west-2"))
//
//        val instanceTag = tags.first { it.key == "InstanceId" }
//
//        println("InstanceTag: $instanceTag")
//
//        val regionTag = tags.first { it.key == "Region" }
//
//        println("RegionTag: $regionTag")

        val bucketName = "sjdsgd-sdkjsdks-sdkusdg-97"

        val POLICY_DOCUMENT = "{\n" +
                "   \"Version\": \"2012-10-17\",\n" +
                "   \"Statement\": [\n" +
                "     {\n" +
                "       \"Effect\": \"Allow\",\n" +
                "       \"Action\": [\"s3:ListBucket\"],\n" +
                "       \"Resource\": [\"arn:aws:s3:::$bucketName\"]\n" +
                "     },\n" +
                "     {\n" +
                "       \"Effect\": \"Allow\",\n" +
                "       \"Action\": [\n" +
                "         \"s3:PutObject\",\n" +
                "         \"s3:GetObject\"\n" +
                "         \"s3:DeleteObject\"\n" +
                "       ],\n" +
                "       \"Resource\": [\"arn:aws:s3:::$bucketName/*\"]\n" +
                "     }\n" +
                "   ]\n" +
                " }"


        println(POLICY_DOCUMENT)
    }

}
