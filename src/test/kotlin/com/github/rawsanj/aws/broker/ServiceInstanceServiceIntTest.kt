package com.github.rawsanj.aws.broker

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.rawsanj.aws.broker.aws.config.AwsConstants
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.AWS_REGION_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.DB_ALLOCATED_STORAGE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_PASSWORD_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.MASTER_USERNAME_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_DB_T2_MICRO_PLAN
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_ENGINE_STRING
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.RDS_SERVICE_ID
import com.github.rawsanj.aws.broker.aws.config.AwsConstants.S3_SERVICE_ID
import com.github.rawsanj.aws.broker.model.STATUS
import com.github.rawsanj.aws.broker.repository.ServiceAuditRepository
import com.github.rawsanj.aws.broker.repository.ServiceInstanceRepository
import org.awaitility.Awaitility
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class ServiceInstanceServiceIntTest {

    private val LOG = LoggerFactory.getLogger(ServiceInstanceServiceIntTest::class.java)

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var serviceInstanceRepository: ServiceInstanceRepository

    @Autowired
    private lateinit var serviceAuditRepository: ServiceAuditRepository


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
            masterUsername = "123InvalidUserName"
        }
    }

    @Before
    fun setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply { SecurityMockMvcConfigurers.springSecurity() }
                .build()
    }

    @Test
    fun `Assert that Invalid RDS request fails to create Instance in AWS`() {

        val reqParams = mutableMapOf<String, Any>()
        reqParams[MASTER_USERNAME_STRING] = masterUsername
        reqParams[DB_ALLOCATED_STORAGE_STRING] = 10
        reqParams[RDS_ENGINE_STRING] = "NOT-MYSQL"
        reqParams[MASTER_PASSWORD_STRING] = UUID.randomUUID().toString()
        reqParams[AWS_REGION_STRING] = "us-west-2"

        val serviceInstanceRequest = ServiceRequest(RDS_SERVICE_ID, RDS_DB_T2_MICRO_PLAN, reqParams)
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceInstanceRequest)

        mvc.perform(MockMvcRequestBuilders.put("/v2/service_instances/${serviceInstanceId}")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))

        val notReadyParams = getServiceInstanceParams(serviceInstanceRepository, serviceInstanceId)

        assertThat(notReadyParams[AwsConstants.HOSTNAME_STRING], CoreMatchers.nullValue())
        assertThat(notReadyParams[AwsConstants.PORT_STRING], CoreMatchers.nullValue())
        assertThat(notReadyParams[AwsConstants.MASTER_USERNAME_STRING].toString(), CoreMatchers.equalTo(masterUsername))
        assertThat(notReadyParams[AwsConstants.MASTER_PASSWORD_STRING], CoreMatchers.notNullValue())

        setDefaultsForAwaitility()

        LOG.info("Waiting for AWS Error Handler Callback")

        Awaitility.await()
                .atMost(10, TimeUnit.MINUTES)
                .until {
                    waitTillAsyncHandlerIsProcessed(serviceInstanceId)
                }

        LOG.info("Resuming test.")

        val serviceAuditCount = serviceAuditRepository.findByStatus(STATUS.FAILED).size
        assertThat(serviceAuditCount, CoreMatchers.notNullValue())

    }


    @Test
    fun `Assert that Request with Invalid ServiceId throws Exception`() {

        val reqParams = mutableMapOf<String, Any>()
        reqParams[MASTER_USERNAME_STRING] = masterUsername

        val serviceId = "INVALID-SERVICE-ID"
        val serviceInstanceRequest = ServiceRequest(serviceId, RDS_DB_T2_MICRO_PLAN, reqParams)
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceInstanceRequest)

        mvc.perform(MockMvcRequestBuilders.put("/v2/service_instances/${serviceInstanceId}")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.description").isNotEmpty)
                .andExpect(jsonPath("$.description").isString)
                .andDo(print())
                .andExpect(jsonPath("$.description").value("Service definition does not exist: id=$serviceId"))
    }

    @Test
    fun `Assert that RDS Request with Invalid PlanId throws Exception`() {

        val reqParams = mutableMapOf<String, Any>()
        reqParams[MASTER_USERNAME_STRING] = masterUsername

        val planId = "INVALID-PLAN-ID"
        val serviceInstanceRequest = ServiceRequest(RDS_SERVICE_ID, planId, reqParams)
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceInstanceRequest)

        mvc.perform(MockMvcRequestBuilders.put("/v2/service_instances/${serviceInstanceId}")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.description").isNotEmpty)
                .andExpect(jsonPath("$.description").isString)
                .andExpect(jsonPath("$.description").value("$planId is not Offered under RDS Services! Available Plans are <db.t2.micro>, <db.t2.medium>, <db.t2.large>"))
    }


    @Test
    fun `Assert that S3 Request with Invalid PlanId throws Exception`() {

        val reqParams = mutableMapOf<String, Any>()
        reqParams[MASTER_USERNAME_STRING] = masterUsername

        val planId = "INVALID-PLAN-ID"
        val serviceInstanceRequest = ServiceRequest(S3_SERVICE_ID, planId, reqParams)
        val objMapper = ObjectMapper()
        val jsonReq = objMapper.writeValueAsString(serviceInstanceRequest)

        mvc.perform(MockMvcRequestBuilders.put("/v2/service_instances/${serviceInstanceId}")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonReq))
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.description").isNotEmpty)
                .andExpect(jsonPath("$.description").isString)
                .andExpect(jsonPath("$.description").value("$planId is not Offered under S3 Bucket Services! Available Plan is <s3-bucket>"))
    }

    private fun waitTillAsyncHandlerIsProcessed(serviceInstanceId: String): Boolean {

        LOG.info("Checking if request for AWS RDS Instance - ServiceIntanceId: $serviceInstanceId is processed.")
        val serviceAuditCount = serviceAuditRepository.findByStatus(STATUS.FAILED).size
        return (serviceAuditCount > 0)

    }
}