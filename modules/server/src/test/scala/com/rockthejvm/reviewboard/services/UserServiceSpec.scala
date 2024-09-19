package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.*
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.repositories.UserRepository
import com.rockthejvm.reviewboard.services.*
import zio.*
import zio.test.*

object UserServiceSpec extends ZIOSpecDefault {

  val testUser = User(
    1L,
    "test@gmail.com",
    "1000:B9FF911AFD0BCA45F30C962E5B3359E9C68D070F1DC65B92:DCAD56A8A6C56D2FD3F6826FBBECDF06CFB8A5F6BB50A311"
  )

  val stubRepoLayer = ZLayer.succeed {
    new UserRepository {
      val db = collection.mutable.Map[Long, User](1L -> testUser)
      override def create(user: User): Task[User] = ZIO.succeed {
        db += (user.id -> user)
        user
      }

      override def getById(id: Long): Task[Option[User]] = ZIO.succeed(db.get(id))

      override def getByEmail(email: String): Task[Option[User]] =
        ZIO.succeed(db.values.find(_.email == email))

      override def update(id: Long, op: User => User): Task[User] = ZIO.attempt {
        val newUser = op(db(id))
        db += (newUser.id -> newUser)
        newUser
      }

      override def delete(id: Long): Task[User] = ZIO.attempt {
        val user = db(id)
        db -= id
        user
      }
    }
  }

  val stubJwtLayer = ZLayer.succeed {
    new JWTService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(user.email, "TOKEN", Long.MaxValue))

      override def verifyToken(token: String): Task[UserID] =
        ZIO.succeed(UserID(testUser.id, testUser.email))
    }
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserServiceSpec")(
      test("create and validate a user") {
        for {
          service  <- ZIO.service[UserService]
          user     <- service.registerUser(testUser.email, "Hello world")
          verified <- service.verifyPassword(testUser.email, "Hello world")
        } yield assertTrue(verified && user.email == testUser.email)
      },
      test("validate correct credentials") {
        for {
          service <- ZIO.service[UserService]
          valid   <- service.verifyPassword(testUser.email, "Hello world")
        } yield assertTrue(valid)
      },
      test("validate incorrect credentials") {
        for {
          service <- ZIO.service[UserService]
          valid   <- service.verifyPassword("random@gmail.com", "Wrong")
        } yield assertTrue(!valid)
      },
      test("update password") {
        for {
          service  <- ZIO.service[UserService]
          newUser  <- service.updatePassword(testUser.email, "Hello world", "newPass")
          oldValid <- service.verifyPassword(testUser.email, "Hello world")
          newValid <- service.verifyPassword(testUser.email, "newPass")
        } yield assertTrue(!oldValid && newValid)
      },
      test("delete non existent user") {
        for {
          service <- ZIO.service[UserService]
          err     <- service.deleteUser("random@gmail.com", "something").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete incorrect pass user") {
        for {
          service <- ZIO.service[UserService]
          err     <- service.deleteUser(testUser.email, "something").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete correct user") {
        for {
          service <- ZIO.service[UserService]
          user    <- service.deleteUser(testUser.email, "Hello world")
        } yield assertTrue(user.email == testUser.email)
      }
    ).provide(
      UserServiceLive.layer,
      stubJwtLayer,
      stubRepoLayer
    )
}
