package com.github.rawsanj.aws.broker

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles(profiles = arrayOf("non-async", "dev"))
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class S3ServiceIntTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mvc: MockMvc;

    companion object {
        @JvmStatic
        private lateinit var serviceInstanceId: String
        @JvmStatic
        private lateinit var bindingId: String
        @JvmStatic
        private lateinit var bucketName: String

        @JvmStatic
        @BeforeClass
        fun initData() {
            serviceInstanceId = UUID.randomUUID().toString()
            bindingId = UUID.randomUUID().toString()
            bucketName = UUID.randomUUID().toString()
        }
    }

    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply { springSecurity() }
                .build()
    }

    @Test
    fun `A - Create S3 ServiceInstance`() {

        val serviceInstanceRequest = ServiceRequest("s3-service", "s3-bucket", mutableMapOf("bucketName" to bucketName))
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceInstanceRequest)

        mvc.perform(put("/v2/service_instances/$serviceInstanceId")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
    }

    @Test
    fun `B - Create S3 Service Binding`() {

        val serviceBindingRequest = ServiceRequest("s3-service", "s3-bucket", emptyMap())
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceBindingRequest)

        mvc.perform(put("/v2/service_instances/$serviceInstanceId/service_bindings/$bindingId")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.credentials.bucketName").value(bucketName))
                .andExpect(jsonPath("$.credentials.AWS_ACCESS_KEY").isNotEmpty)
                .andExpect(jsonPath("$.credentials.AWS_SECRET_KEY").isNotEmpty)
                .andDo(print())
    }

    @Test
    fun `C - Delete S3 Service Binding`() {

        val serviceBindingRequest = ServiceRequest("s3-service", "s3-bucket", emptyMap())

        mvc.perform(delete("/v2/service_instances/$serviceInstanceId/service_bindings/$bindingId")
                .param("service_id", serviceBindingRequest.service_id)
                .param("plan_id", serviceBindingRequest.plan_id))
                .andExpect(status().isOk)
                .andDo(print())
    }

    @Test
    fun `D - Delete S3 Service Instance`() {

        val serviceInstanceRequest = ServiceRequest("s3-service", "s3-bucket", emptyMap())

        mvc.perform(delete("/v2/service_instances/$serviceInstanceId")
                .param("service_id", serviceInstanceRequest.service_id)
                .param("plan_id", serviceInstanceRequest.plan_id))
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
    }
}
