package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.services.JWTService
import zio.*
import zio.test.*

object JWTServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("JWTServiceSpec")(
      test("create and validate token") {
        for {
          service <- ZIO.service[JWTService]
          token   <- service.createToken(User(1L, "test@gmail.com", "password"))
          userId  <- service.verifyToken(token.token)
        } yield assertTrue(userId.id == 1L && userId.email == "test@gmail.com")
      }
    ).provide(
      JWTServiceLive.layer,
      ZLayer.succeed(JWTConfig("secret", 3600))
    )
}
