package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

trait ReviewRepository {
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(id: Long): Task[List[Review]]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]
}

class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {

  import quill.*

  inline given schema: SchemaMeta[Review] = schemaMeta[Review]("reviews")

  inline given insMeta: InsertMeta[Review] = insertMeta[Review](_.id)

  inline given upMeta: UpdateMeta[Review] = updateMeta[Review](_.id)

  def create(review: Review): Task[Review] =
    run {
      query[Review]
        .insertValue(lift(review))
        .returning(r => r)
    }

  def getById(id: Long): Task[Option[Review]] = run {
    query[Review].filter(_.id == lift(id))
  }.map(_.headOption)

  def getByCompanyId(id: Long): Task[List[Review]] =
    ZIO.fail(new RuntimeException("Not implemented"))

  def getByUserId(id: Long): Task[List[Review]] = ZIO.fail(new RuntimeException("Not implemented"))

  def update(id: Long, op: Review => Review): Task[Review] =
    ZIO.fail(new RuntimeException("Not implemented"))

  def delete(id: Long): Task[Review] = ZIO.fail(new RuntimeException("Not implemented"))
}

object ReviewRepositoryLive {
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase.type]].map(quill => new ReviewRepositoryLive(quill))
  }
}
