package io.sourceempire.brawlpong.auth

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import io.sourceempire.brawlpong.models.JwtMatchAuthInfo
import java.util.*

private const val brawlJwtKey = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzyLwQf73XW5wiojO0IlE+w+xguHQzD7lSdPAaNQG+g8BOvBd7jkuLlGY+WeyJBE2lGDCiQzdypDOMG16u5xCW1U5oiHaCjYIAnQfPVl/gF7mYtcpFcHMsvrCbDznSP9OQgbJa8qE8hxYkvrF+f7+A9BPlfEN56bpqGjporrkpxx/BWz9ftaTaqEQkqoSr2tbCdbVsS5ojRBp2z+ho5Q53O9S/+ixFR1Kjz8daYQ1thQJK+475GJ+nR/h4GmxXrWPd5F27REyPRNX5fodLg7VNR+BuGtsuOGkoK8RpSG9cfDmkh7efIBpVwRw3n67zigGs8IVkGCFgxGaH5X/Ip5UkQIDAQAB\n-----END PUBLIC KEY-----"

class Auth(vertx: Vertx) {
    private val jwtAuth: JWTAuth

    init {
        val jwtAuthConfig = jwtAuthOptionsOf(
            pubSecKeys = listOf(
                pubSecKeyOptionsOf(algorithm = "RS256",
                    buffer = Buffer.buffer(brawlJwtKey)
                )
            )
        )

        jwtAuth = JWTAuth.create(vertx, jwtAuthConfig)
    }

    /**
     * Validates a Brawl JWT and checks if the "use" claim is set to "pong".
     *
     * This function attempts to authenticate the provided JWT using the jwtAuth instance.
     * If the authentication is successful, it checks if the "use" claim in the JWT is set to "pong".
     * If the "use" claim matches, the function completes the promise with the JWT's claims as a JsonObject.
     * If the "use" claim does not match or the authentication fails, the function fails the promise with an error message.
     *
     * @param jwt The JWT string to be validated.
     * @return A future containing the JWT's claims as a JsonObject if the validation is successful and the "use" claim is "pong"; otherwise, an error message.
     */
    fun validateBrawlToken(jwt: String): Future<JwtMatchAuthInfo> {
        return jwtAuth.authenticate(TokenCredentials(jwt))
            .compose { claims ->
                if (claims.containsKey("use") &&
                    claims.get<String>("use") == "pong" &&
                    claims.containsKey("playerId") &&
                    claims.containsKey("matchId")) {

                    val playerId = UUID.fromString(claims.get("playerId"))
                    val matchId = UUID.fromString(claims.get("matchId"))

                    Future.succeededFuture(JwtMatchAuthInfo(playerId, matchId))
                }
                else {
                    Future.failedFuture("Not correct type of use")
                }
            }.recover {
                it.printStackTrace()
                Future.failedFuture("Not valid jwt")
            }
    }
}