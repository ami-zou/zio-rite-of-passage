package com.rockthejvm.reviewboard.services

import com.auth0.jwt.*
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.rockthejvm.reviewboard.domain.data.*
import zio.*

import java.time.Instant

trait JWTService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserID]
}

class JWTServiceLive(clock: java.time.Clock) extends JWTService {
  private val algorithm      = Algorithm.HMAC512("secret")
  private val TTL            = 30 * 24 * 3600
  private val ISSUER         = "rockthejvm.com"
  private val CLAIM_USERNAME = "username"

  private val verifier: JWTVerifier =
    JWT
      .require(algorithm)
      .withIssuer(ISSUER)
      .asInstanceOf[BaseVerification]
      .build(clock)

  override def createToken(user: User): Task[UserToken] =
    for {
      now        <- ZIO.attempt(clock.instant())
      expiration <- ZIO.succeed(now.plusSeconds(TTL))
      token <- ZIO.attempt(
        JWT
          .create()
          .withIssuer(ISSUER)
          .withIssuedAt(now)
          .withExpiresAt(expiration)
          .withSubject(user.id.toString)
          .withClaim(CLAIM_USERNAME, user.email)
          .sign(algorithm)
      )
    } yield UserToken(user.email, token, expiration.getEpochSecond())

  override def verifyToken(token: String): Task[UserID] =
    for {
      decoded <- ZIO.attempt(verifier.verify(token))
      userId <- ZIO.attempt(
        UserID(decoded.getSubject().toLong, decoded.getClaim(CLAIM_USERNAME).asString())
      )
    } yield userId
}

object JWTServiceLive {
  val layer = ZLayer {
    Clock.javaClock.map(clock => new JWTServiceLive(clock))
  }
}

object JWTServiceDemo extends ZIOAppDefault {
  val program = for {
    service <- ZIO.service[JWTService]
    token   <- service.createToken(User(1L, "email", "password"))
    _       <- Console.printLine(token.token)
    userId  <- service.verifyToken(token.token)
    _       <- Console.printLine(userId.toString)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program.provide(
      JWTServiceLive.layer
    )
}
