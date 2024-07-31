package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.repositories.*
import zio.*

trait CompanyService {
  def create(request: CreateCompanyRequest): Task[Company]
  def getById(id: String): Task[Option[Company]]
  def getAll(): Task[List[Company]]
}

class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService {
  override def create(request: CreateCompanyRequest): Task[Company] =
    repo.create(request.toCompany(-1L))

  override def getAll(): Task[List[Company]] = repo.getAll()

  override def getById(id: String): Task[Option[Company]] = repo.getById(id)
}

object CompanyServiceLive {
  val layer = ZLayer {
    ZIO.service[CompanyRepository].map(repo => CompanyServiceLive(repo))
  }
}
