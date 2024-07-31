package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

trait CompanyRepository {
  def create(company: Company): Task[Company]
  def getById(id: String): Task[Option[Company]]
  def getAll(): Task[List[Company]]
}

class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {
  override def create(company: Company): Task[Company] = ???

  override def getById(id: String): Task[Option[Company]] = ???

  override def getAll(): Task[List[Company]] = ???
}

object CompanyRepositoryLive {
  val layer = ZLayer {
    ZIO
      .service[Quill.Postgres[SnakeCase.type]]
      .map(quill => new CompanyRepositoryLive(quill))
  }
}
