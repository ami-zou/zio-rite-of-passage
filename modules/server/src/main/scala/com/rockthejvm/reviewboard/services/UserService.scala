package com.rockthejvm.reviewboard.services
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.repositories.UserRepository
import zio.*

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User]
  def deleteUser(email: String, password: String): Task[User]
  // JWT
  def generateToken(email: String, password: String): Task[Option[UserToken]]
}

class UserServiceLive private (jwtService: JWTService, userRepo: UserRepository)
    extends UserService {
  override def registerUser(email: String, password: String): Task[User] =
    userRepo.create(
      User(
        id = -1L,
        email = email,
        hashedPassword = UserServiceLive.Hasher.generateHash(password)
      )
    )

  override def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User] =
    for {
      existingUser <- userRepo
        .getByEmail(email)
        .someOrFail(new RuntimeException(s"Cannot verify user $email - user not found"))
      verified <- ZIO.attempt(
        UserServiceLive.Hasher.validateHash(oldPassword, existingUser.hashedPassword)
      )
      updatedUser <- userRepo
        .update(
          existingUser.id,
          user =>
            user.copy(
              hashedPassword = UserServiceLive.Hasher.generateHash(newPassword)
            )
        )
        .when(verified)
        .someOrFail(new RuntimeException(s"Could not update password for $email"))
    } yield updatedUser

  override def deleteUser(email: String, password: String): Task[User] = {
    for {
      existingUser <- userRepo
        .getByEmail(email)
        .someOrFail(new RuntimeException(s"Cannot verify user $email - user not found"))
      verified <- ZIO.attempt(
        UserServiceLive.Hasher.validateHash(password, existingUser.hashedPassword)
      )
      deletedUser <- userRepo
        .delete(existingUser.id)
        .when(verified)
        .someOrFail(new RuntimeException(s"Could not update password for $email"))
    } yield deletedUser
  }

  override def verifyPassword(email: String, password: String): Task[Boolean] =
    for {
      existingUser <- userRepo.getByEmail(email)
      result <- existingUser match {
        case None => ZIO.succeed(false)
        case Some(user) =>
          ZIO
            .attempt(UserServiceLive.Hasher.validateHash(password, user.hashedPassword))
            .orElseSucceed(false)
      }
    } yield result

  override def generateToken(email: String, password: String): Task[Option[UserToken]] = {
    for {
      existingUser <- userRepo
        .getByEmail(email)
        .someOrFail(new RuntimeException(s"Cannot verify user $email - user not found"))
      verified <- ZIO.attempt(
        UserServiceLive.Hasher.validateHash(password, existingUser.hashedPassword)
      )
      maybeToken <- jwtService.createToken(existingUser).when(verified)
    } yield maybeToken
  }
}

object UserServiceLive {
  val layer = ZLayer {
    for {
      jwtService <- ZIO.service[JWTService]
      userRepo   <- ZIO.service[UserRepository]
    } yield new UserServiceLive(jwtService, userRepo)
  }

  object Hasher {
    // private
    private val PBKDF2_ALGORITHM: String = "PBKDF2WithHmacSHA512"
    private val PBKDF2_ITERATIONS: Int   = 1000
    private val SALT_BYTE_SIZE: Int      = 24
    private val HASH_BYTE_SIZE: Int      = 24
    private val skf: SecretKeyFactory    = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

    private def pbkdf2(
        message: Array[Char],
        salt: Array[Byte],
        iterations: Int,
        nBytes: Int
    ): Array[Byte] = {
      val keySpec: PBEKeySpec = new PBEKeySpec(message, salt, iterations, nBytes * 8)
      skf.generateSecret(keySpec).getEncoded
    }

    private def toHex(array: Array[Byte]): String = array.map(b => "%02X".format(b)).mkString
    // hex-encoded bytes, more readable

    private def fromHex(string: String): Array[Byte] =
      string.sliding(2, 2).toArray.map(hexValue => Integer.parseInt(hexValue, 16).toByte)

    // a(i) ^ b(i) for every i (exclusive OR, XOR)
    private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean = {
      val range = 0 until math.min(a.length, b.length)
      val diff = range.foldLeft(a.length ^ b.length) { case (acc, i) =>
        acc | (a(i) ^ b(i))
      }
      diff == 0
    }

    def generateHash(string: String): String = {
      val rng: SecureRandom = new SecureRandom()
      val salt: Array[Byte] = Array.ofDim[Byte](SALT_BYTE_SIZE)
      rng.nextBytes(salt) // creates 24 random bytes
      val hashBytes = pbkdf2(string.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
      // string + salt + number of iterations PBKDF2
      // "1000:AAAAAAAAAAA:BBBBBBBBB"
      s"$PBKDF2_ITERATIONS:${toHex(salt)}:${toHex(hashBytes)}"
    }

    def validateHash(string: String, hash: String): Boolean = {
      val hashSegments = hash.split(":")
      val nIterations  = hashSegments(0).toInt
      val salt         = fromHex(hashSegments(1))
      val expectedHash = fromHex(hashSegments(2))
      val actualHash   = pbkdf2(string.toCharArray(), salt, nIterations, HASH_BYTE_SIZE)
      compareBytes(actualHash, expectedHash)
    }
  }
}

object UserServiceDemo {
  def main(args: Array[String]) =
    println(UserServiceLive.Hasher.generateHash("Hello world"))
    println(
      UserServiceLive.Hasher.validateHash(
        "Hello world",
        "1000:B9FF911AFD0BCA45F30C962E5B3359E9C68D070F1DC65B92:DCAD56A8A6C56D2FD3F6826FBBECDF06CFB8A5F6BB50A311"
      )
    )
}
