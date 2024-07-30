package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import zio.*
import zio.test.*

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  private val rtjvm = Company(1L, "rock-the-jvm", "Rock the JVM", "rockth ejvm.com")

  private def genString() = scala.util.Random.alphanumeric.take(8).mkString

  private def genCompany(): Company =
    Company(
      id = -1L,
      slug = genString(),
      name = genString(),
      url = genString()
    )

  override val initScript: String = "sql/companies.sql"

  override def spec: Spec[TestEnvironment with Scope, Any] = ???
}
