package com.project.myservice.presentation.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.myservice.application.user.UserService
import com.project.myservice.common.exception.UserNotFoundException
import com.project.myservice.common.util.toLocalString
import com.project.myservice.domain.user.UserDetailInfo
import com.project.myservice.domain.user.UserInfo
import com.sun.security.auth.UserPrincipal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.Instant


internal class UserControllerTest {

    @Nested
    @WebMvcTest(UserController::class)
    @ExtendWith(RestDocumentationExtension::class)
    @DisplayName("???????????? API??? ????????? ???")
    inner class RequestCreateUserApi {

        lateinit var mockMvc: MockMvc

        @MockBean
        lateinit var userServiceMock: UserService

        @Autowired
        lateinit var objectMapper: ObjectMapper

        @BeforeEach
        fun setUp(webApplicationContext: WebApplicationContext, restDocumentation: RestDocumentationContextProvider) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build()
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        fun `????????? ???????????? ????????? ??? ??????`(username: String?) {
            // given
            val request = CreateUserRequestDto(
                username = username,
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                    .value("????????? 'username' ?????? ???????????? ????????????. ?????? ???: '$username'. ????????? ???????????? ????????? ??? ????????????"))
        }

        @Test
        fun `????????? ???????????? 25????????? ????????????`() {
            // given
            val request = CreateUserRequestDto(
                username = "12345678901234567890123456",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'username' ?????? ???????????? ????????????. ?????? ???: '12345678901234567890123456'. ????????? ???????????? ?????? 25????????? ???????????????"))
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        fun `???????????? ????????? ??? ??????`(email: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = email,
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'email' ?????? ???????????? ????????????. ?????? ???: '$email'. ???????????? ????????? ??? ????????????"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["test.com", "test@test@test.com"])
        fun `????????? ????????? ????????? ????????? ??????`(email: String) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = email,
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'email' ?????? ???????????? ????????????. ?????? ???: '$email'. ????????? ????????? ????????? ????????????"))
        }

        @Test
        fun `???????????? ?????? 50????????? ????????????`() {
            // given
            val email = "123456789012345678901234567890123456789012@test.com"
            val request = CreateUserRequestDto(
                username = "testusername",
                email = email,
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'email' ?????? ???????????? ????????????. ?????? ???: '$email'. ???????????? ?????? 50????????? ???????????????"))
        }

        @ParameterizedTest
        @NullSource
        fun `??????????????? ????????? ??? ??????`(phoneNumber: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = phoneNumber,
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'phoneNumber' ?????? ???????????? ????????????. ?????? ???: '$phoneNumber'. ??????????????? ????????? ??? ????????????"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["010", "010111122223", "0101111222a"])
        fun `??????????????? 10~11?????? ????????? ?????? ????????????`(phoneNumber: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = phoneNumber,
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'phoneNumber' ?????? ???????????? ????????????. ?????? ???: '$phoneNumber'. ??????????????? 10~11?????? ????????? ?????? ???????????????"))
        }

        @ParameterizedTest
        @NullSource
        fun `??????????????? ????????? ??? ??????`(password: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = password,
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'password' ?????? ???????????? ????????????. ?????? ???: '$password'. ??????????????? ????????? ??? ????????????"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["7digit0", "21digit00000000000000", "Testpassword01", "Testpassword!#", "testpassword0!#", "TESTPASSWORD0!#"])
        fun `??????????????? 8~20????????????, ????????? ????????? ??????,?????????,?????????,??????????????? ??????????????????`(password: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = password,
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'password' ?????? ???????????? ????????????. ?????? ???: '$password'. ??????????????? 8~20????????????, ????????? ????????? ??????,?????????,?????????,??????????????? ?????????????????????"))
        }

        @ParameterizedTest
        @NullSource
        fun `????????? ????????? ????????? ??? ??????`(name: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = name,
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'name' ?????? ???????????? ????????????. ?????? ???: '$name'. ????????? ????????? ????????? ??? ????????????"))
        }

        @Test
        fun `????????? ????????? ?????? 50????????? ????????????`() {
            // given
            val name = "123456789012345678901234567890123456789012345678901"
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = name,
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'name' ?????? ???????????? ????????????. ?????? ???: '$name'. ????????? ?????? 50????????? ???????????????"))
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        fun `????????? ????????? ??? ??????`(nickname: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = nickname,
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'nickname' ?????? ???????????? ????????????. ?????? ???: '$nickname'. ????????? ????????? ??? ????????????"))
        }

        @Test
        fun `????????? ?????? 50????????? ????????????`() {
            // given
            val nickname = "123456789012345678901234567890123456789012345678901"
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = nickname,
                authenticationNumber = "1234"
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'nickname' ?????? ???????????? ????????????. ?????? ???: '$nickname'. ????????? ?????? 50????????? ???????????????"))
        }

        @ParameterizedTest
        @NullSource
        fun `??????????????? ????????? ??? ??????`(authenticationNumber: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = authenticationNumber
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'authenticationNumber' ?????? ???????????? ????????????. ?????? ???: '$authenticationNumber'. ??????????????? ????????? ??? ????????????"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["123", "12345", "123a", "123A"])
        fun `??????????????? 4?????? ????????? ????????????`(authenticationNumber: String?) {
            // given
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = authenticationNumber
            )

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'authenticationNumber' ?????? ???????????? ????????????. ?????? ???: '$authenticationNumber'. ??????????????? 4?????? ????????? ?????? ???????????????"))
        }

        @Test
        fun `?????? ???????????? ???????????? ApplicationService??? ????????????, ????????? ???????????? UserInfo??? ????????????`() {
            // given
            val currentTime = Instant.ofEpochMilli(1640999420000) // 2022-01-01 01:10:20 GMT
            val request = CreateUserRequestDto(
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                password = "Test12345!@#$",
                name = "testname",
                nickname = "testnickname",
                authenticationNumber = "1234"
            )

            val userInfo = UserInfo(
                id = 1L,
                username = "testusername",
                email = "test@test.com",
                phoneNumber = "01011112222",
                name = "testname",
                nickname = "testnickname",
                createdAt = currentTime,
                updatedAt = null,
                deletedAt = null
            )

            Mockito.`when`(userServiceMock.createUser(request.toCommand()))
                .thenReturn(userInfo)

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.email").value("test@test.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.phoneNumber").value("01011112222"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.username").value("testusername"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.nickname").value("testnickname"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.createdAt").value("2022-01-01 10:10:20"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.updatedAt").value(null))
                .andExpect(MockMvcResultMatchers.jsonPath("data.deletedAt").value(null))
                .andDo(
                    MockMvcRestDocumentation.document(
                        "users/create",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        PayloadDocumentation.requestFields(
                            PayloadDocumentation.fieldWithPath("username").type(JsonFieldType.STRING).description("????????? ?????????"),
                            PayloadDocumentation.fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                            PayloadDocumentation.fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("????????????"),
                            PayloadDocumentation.fieldWithPath("password").type(JsonFieldType.STRING).description("????????????"),
                            PayloadDocumentation.fieldWithPath("name").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("nickname").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("authenticationNumber").type(JsonFieldType.STRING).description("????????????"),
                        ),
                        PayloadDocumentation.responseFields(
                            PayloadDocumentation.fieldWithPath("result").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                            PayloadDocumentation.fieldWithPath("data.username").type(JsonFieldType.STRING).description("????????? ?????????"),
                            PayloadDocumentation.fieldWithPath("data.email").type(JsonFieldType.STRING).description("?????????"),
                            PayloadDocumentation.fieldWithPath("data.phoneNumber").type(JsonFieldType.STRING).description("????????????"),
                            PayloadDocumentation.fieldWithPath("data.name").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.updatedAt").type(JsonFieldType.NULL).description("????????? ?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.deletedAt").type(JsonFieldType.NULL).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("errorCode").type(JsonFieldType.NULL).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("message").type(JsonFieldType.NULL).description("?????? ?????????"),

                        )
                    )
                )

            Mockito.verify(userServiceMock).createUser(request.toCommand())
        }
    }

    @Nested
    @WebMvcTest(UserController::class)
    @ExtendWith(RestDocumentationExtension::class)
    @DisplayName("???????????? ?????? API??? ????????? ???")
    inner class RequestResetPasswordApi {

        lateinit var mockMvc: MockMvc

        @MockBean
        lateinit var userServiceMock: UserService

        @Autowired
        lateinit var objectMapper: ObjectMapper

        @BeforeEach
        fun setUp(webApplicationContext: WebApplicationContext, restDocumentation: RestDocumentationContextProvider) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build()
        }

        @ParameterizedTest
        @NullSource
        fun `??????????????? ????????? ??? ??????`(phoneNumber: String?) {
            // given
            val request = ResetPasswordRequestDto(phoneNumber, "Secret1323!", "1234")

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/reset-password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'phoneNumber' ?????? ???????????? ????????????. ?????? ???: '$phoneNumber'. ??????????????? ????????? ??? ????????????"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["010", "010111122223", "0101111222a"])
        fun `??????????????? 10~11?????? ????????? ?????? ????????????`(phoneNumber: String?) {
            // given
            val request = ResetPasswordRequestDto(phoneNumber, "Secret1323!", "1234")

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/reset-password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'phoneNumber' ?????? ???????????? ????????????. ?????? ???: '$phoneNumber'. ??????????????? 10~11?????? ????????? ?????? ???????????????"))
        }

        @ParameterizedTest
        @NullSource
        fun `??????????????? ????????? ??? ??????`(newPassword: String?) {
            // given
            val request = ResetPasswordRequestDto("01011112222", newPassword, "1234")

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/reset-password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'newPassword' ?????? ???????????? ????????????. ?????? ???: '$newPassword'. ??????????????? ????????? ??? ????????????"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["7digit0", "21digit00000000000000", "Testpassword01", "Testpassword!#", "testpassword0!#", "TESTPASSWORD0!#"])
        fun `??????????????? 8~20????????????, ????????? ????????? ??????,?????????,?????????,??????????????? ??????????????????`(newPassword: String?) {
            // given
            val request = ResetPasswordRequestDto("01011112222", newPassword, "1234")

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/reset-password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'newPassword' ?????? ???????????? ????????????. ?????? ???: '$newPassword'. ??????????????? 8~20????????????, ????????? ????????? ??????,?????????,?????????,??????????????? ?????????????????????"))
        }

        @ParameterizedTest
        @NullSource
        fun `??????????????? ????????? ??? ??????`(authenticationNumber: String?) {
            // given
            val request = ResetPasswordRequestDto("01011112222", "Secret1323!", authenticationNumber)

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/reset-password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'authenticationNumber' ?????? ???????????? ????????????. ?????? ???: '$authenticationNumber'. ??????????????? ????????? ??? ????????????"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["123", "12345", "123a", "123A"])
        fun `??????????????? 4?????? ????????? ?????? ????????????`(authenticationNumber: String?) {
            // given
            val request = ResetPasswordRequestDto("01011112222", "Secret1323!", authenticationNumber)

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/reset-password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("message")
                        .value("????????? 'authenticationNumber' ?????? ???????????? ????????????. ?????? ???: '$authenticationNumber'. ??????????????? 4?????? ????????? ?????? ???????????????"))
        }

        @Test
        fun `?????? ???????????? ???????????? ApplicationService??? ????????????, ????????? ???????????? ?????? ????????? ????????????`() {
            // given
            val request = ResetPasswordRequestDto("01011112222", "Secret1323!", "1234")

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/users/reset-password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("SUCCESS"))
                .andDo(
                    MockMvcRestDocumentation.document(
                        "users/reset-password",
                        preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        PayloadDocumentation.requestFields(
                            PayloadDocumentation.fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("????????????"),
                            PayloadDocumentation.fieldWithPath("newPassword").type(JsonFieldType.STRING).description("?????? ????????????"),
                            PayloadDocumentation.fieldWithPath("authenticationNumber").type(JsonFieldType.STRING).description("????????????"),
                        ),
                        PayloadDocumentation.responseFields(
                            PayloadDocumentation.fieldWithPath("result").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data").type(JsonFieldType.NULL).description("?????? ?????????"),
                            PayloadDocumentation.fieldWithPath("errorCode").type(JsonFieldType.NULL).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("message").type(JsonFieldType.NULL).description("?????? ?????????"),
                        )
                    )
                )

            Mockito.verify(userServiceMock)
                .resetPassword(request.toCommand())
        }
    }

    @Nested
    @WebMvcTest(UserController::class)
    @ExtendWith(RestDocumentationExtension::class)
    @DisplayName("??? ?????? ????????? ????????? ???")
    inner class FindMyApi {

        lateinit var mockMvc: MockMvc

        @MockBean
        lateinit var userServiceMock: UserService

        @BeforeEach
        fun setUp(webApplicationContext: WebApplicationContext, restDocumentation: RestDocumentationContextProvider) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build()

            val user: UserDetails = org.springframework.security.core.userdetails.User(
                "username",
                "Secret1323!",
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )

            val context = SecurityContextHolder.getContext()
            context.authentication = UsernamePasswordAuthenticationToken(user, user.password, user.authorities)
        }

        @Test
        fun `????????? ????????? ????????? ????????? ????????????`() {
            // given
            val principal = UserPrincipal("testusername")

            Mockito.`when`(userServiceMock.findUserDetail(principal.name))
                .thenThrow(UserNotFoundException())

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/users/my")
                    .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("FAIL"))
                .andExpect(MockMvcResultMatchers.jsonPath("errorCode").value("USER_NOT_FOUND"))
        }

        @Test
        fun `????????? ???????????????, ?????? ????????? ????????????`() {
            // given
            val principal = UserPrincipal("testusername")
            val currentTime = Instant.now()

            val userDetailInfo = UserDetailInfo(
                1L,
                "testusername",
                "test@test.com",
                "01011112222",
                "testname",
                "testnickname",
                listOf("ROLE_USER"),
                currentTime,
                currentTime,
                null
            )

            Mockito.`when`(userServiceMock.findUserDetail(principal.name))
                .thenReturn(userDetailInfo)

            // when, then
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/users/my")
                    .header("Authorization", "bearer accessToken")
                    .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo { MockMvcResultHandlers.print() }
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.username").value("testusername"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.email").value("test@test.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.phoneNumber").value("01011112222"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.name").value("testname"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.nickname").value("testnickname"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.roles[0]").value("ROLE_USER"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.createdAt").value(currentTime.toLocalString()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.updatedAt").value(currentTime.toLocalString()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.deletedAt").value(null))
                .andDo(
                    MockMvcRestDocumentation.document(
                        "users/my",
                        preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        HeaderDocumentation.requestHeaders(headerWithName("Authorization").description("bearer token")),
                        PayloadDocumentation.responseFields(
                            PayloadDocumentation.fieldWithPath("result").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                            PayloadDocumentation.fieldWithPath("data.username").type(JsonFieldType.STRING).description("????????? ?????????"),
                            PayloadDocumentation.fieldWithPath("data.email").type(JsonFieldType.STRING).description("?????????"),
                            PayloadDocumentation.fieldWithPath("data.phoneNumber").type(JsonFieldType.STRING).description("????????????"),
                            PayloadDocumentation.fieldWithPath("data.name").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.roles").type(JsonFieldType.ARRAY).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("????????? ?????? ??????"),
                            PayloadDocumentation.fieldWithPath("data.deletedAt").type(JsonFieldType.NULL).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("errorCode").type(JsonFieldType.NULL).description("?????? ??????"),
                            PayloadDocumentation.fieldWithPath("message").type(JsonFieldType.NULL).description("?????? ?????????"),
                        )
                    )
                )
        }
    }
}