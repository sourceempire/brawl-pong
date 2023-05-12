package auth

import io.sourceempire.brawlpong.auth.Auth
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
class AuthTest {

    private lateinit var auth: Auth

    @BeforeAll
    fun beforeAll() {
        auth = Auth(Vertx.vertx())
    }

//    @Test
//    fun verifyBrawlJWT(context: VertxTestContext) {
//        val jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJtYXRjaElkIjoiOTEyZjA3NmUtZTc5Ni00ZGUwLTgyMTgtMDNjZWYyMWMzMjcwIiwidXNlcklkIjoiN2NiZTE4NzYtN2IzOS00YmZkLWIzYjctMDcwYzk1MjUxODY5IiwidXNlIjoicG9uZyIsImlhdCI6MTY4MzMwNTQzOCwiZXhwIjoxNjgzMzA2MDM4fQ.s9EyuTeNqhkeP7o5uIU6XjTQO6-DPy6-Us6EMHOnHghVQk93Dnjq7MvSqgokp1Ge21WF1r1rxjH1_56ZWfgJNnIzXZiAJf4h3XO2Te_M48Nih1huEQb-CeijFUNWuGnSYtht48BysQT2iUV1GMF1V0YhSXNdgda8-o7BMT17kXiXPNpQUWMm-Y9k3okkfDaq6Zds3dGI0Ov_AwFPrr1lhtHIRddgHXE7SOE7OUi44a6vIXHbkTrrFJni3itA3hBt2I2pDjztepGAe2QAZosKKxT_48GnHsQnQ7NC4fFFdQosvs20l7mhrpqlJj-FbPMk4jHyaPM9yqQbKl_HCb-Zhg"
//
//        auth.validateBrawlKey(jwt, "pong")
//            .onSuccess {
//                println(it.encodePrettily())
//                context.completeNow()
//            }.onFailure {
//                context.failNow(it)
//            }
//
//    }
}