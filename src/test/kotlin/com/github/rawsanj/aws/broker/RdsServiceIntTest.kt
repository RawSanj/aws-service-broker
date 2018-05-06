package com.github.rawsanj.aws.broker

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.rawsanj.aws.broker.TestConstants.PLAN_ID_STRING
import com.github.rawsanj.aws.broker.TestConstants.SERVICE_ID_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.HOSTNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.PORT_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_MICRO_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_SERVICE_ID
import com.github.rawsanj.aws.broker.repository.ServiceInstanceRepository
import org.awaitility.Awaitility.*
import org.awaitility.Duration
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
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
import java.util.concurrent.TimeUnit

@RunWith(SpringRunner::class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RdsServiceIntTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var serviceInstanceRepository: ServiceInstanceRepository

    private lateinit var mvc: MockMvc;

    companion object {
        @JvmStatic
        private lateinit var serviceInstanceId: String
        @JvmStatic
        private lateinit var bindingId: String
        @JvmStatic
        private lateinit var masterUsername: String

        @JvmStatic
        @BeforeClass
        fun initData() {
            serviceInstanceId = UUID.randomUUID().toString()
            bindingId = UUID.randomUUID().toString()
            masterUsername = "AwsRDSDBAdmin"
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
    fun `A - Create RDS Service Instance`() {

        val serviceInstanceRequest = ServiceRequest(RDS_SERVICE_ID, RDS_DB_T2_MICRO_PLAN, mutableMapOf("masterUsername" to masterUsername))
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceInstanceRequest)

        mvc.perform(put("/v2/service_instances/$serviceInstanceId")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())

        val notReadyParams = getServiceInstanceParams()

        assertThat(notReadyParams[HOSTNAME_STRING], nullValue())
        assertThat(notReadyParams[PORT_STRING], nullValue())
        assertThat(notReadyParams[MASTER_USERNAME_STRING].toString(), equalTo(masterUsername))
        assertThat(notReadyParams[MASTER_PASSWORD_STRING], notNullValue())

        setDefaultsForAwaitility()

        println("Waiting for RDS Instance to be Ready")

        await()
                .atMost(10, TimeUnit.MINUTES)
                .until {
                    waitTillRdsDBisReady(serviceInstanceId)
                }

        println("RDS is ready Now, resuming test.")

        val params = getServiceInstanceParams()

        assertThat(params[HOSTNAME_STRING], notNullValue())
        assertThat(params[PORT_STRING].toString(), equalTo("3306"))
        assertThat(params[MASTER_USERNAME_STRING].toString(), equalTo(masterUsername))
        assertThat(params[MASTER_PASSWORD_STRING], notNullValue())

    }

    @Test
    fun `B - Create RDS Service Binding`() {

        val serviceBindingRequest = ServiceRequest(RDS_SERVICE_ID, RDS_DB_T2_MICRO_PLAN, emptyMap())
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceBindingRequest)

        mvc.perform(put("/v2/service_instances/$serviceInstanceId/service_bindings/$bindingId")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isCreated)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.credentials.username").value(masterUsername))
                .andExpect(jsonPath("$.credentials.password").isNotEmpty)
                .andExpect(jsonPath("$.credentials.hostname").isNotEmpty)
                .andExpect(jsonPath("$.credentials.port").isNotEmpty)
                .andDo(print())

    }

    @Test
    fun `C - Delete RDS Service Binding`() {

        val serviceBindingRequest = ServiceRequest(RDS_SERVICE_ID, RDS_DB_T2_MICRO_PLAN, emptyMap())

        mvc.perform(delete("/v2/service_instances/$serviceInstanceId/service_bindings/$bindingId")
                .param(SERVICE_ID_STRING, serviceBindingRequest.service_id)
                .param(PLAN_ID_STRING, serviceBindingRequest.plan_id))
                .andExpect(status().isOk)
                .andDo(print())
    }

    @Test
    fun `D - Delete RDS Service Instance`() {

        val serviceInstanceRequest = ServiceRequest(RDS_SERVICE_ID, RDS_DB_T2_MICRO_PLAN, emptyMap())

        mvc.perform(delete("/v2/service_instances/$serviceInstanceId")
                .param(SERVICE_ID_STRING, serviceInstanceRequest.service_id)
                .param(PLAN_ID_STRING, serviceInstanceRequest.plan_id))
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
    }

    private fun waitTillRdsDBisReady(serviceInstanceId: String): Boolean {

        println("Checking if AWS RDS is ready.")

        val serviceInstance = serviceInstanceRepository.findById(serviceInstanceId)

        return if (serviceInstance.isPresent) {
            serviceInstance.get().parameters.containsKey(HOSTNAME_STRING)
        } else {
            false
        }
    }

    private fun setDefaultsForAwaitility() {
        setDefaultPollInterval(30, TimeUnit.SECONDS);
        setDefaultPollDelay(Duration.ONE_MINUTE);
        setDefaultTimeout(Duration.ONE_MINUTE);
    }

    private fun getServiceInstanceParams(): Map<String, Any> {
        val serviceInstance = serviceInstanceRepository.findById(serviceInstanceId).get()
        return serviceInstance.parameters
    }

}
