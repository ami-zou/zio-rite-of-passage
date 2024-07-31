package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.endpoints.*
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.services.{CompanyService, ReviewService}
import sttp.tapir.Endpoint
import sttp.tapir.server.ServerEndpoint
import zio.*

import scala.collection.mutable

class CompanyController private (companyService: CompanyService)
    extends BaseController
    with CompanyEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogicSuccess(req => companyService.create(req))

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogicSuccess(id => companyService.getById(id))

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoints.serverLogicSuccess(_ => companyService.getAll())

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)
}

object CompanyController {
  val makeZIO =
    ZIO.service[CompanyService].map(companyService => new CompanyController(companyService))
}
