package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.*
import com.rockthejvm.reviewboard.services.CompanyService
import sttp.tapir.server.ServerEndpoint
import zio.*

class CompanyController private (companyService: CompanyService)
    extends BaseController
    with CompanyEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic(req => companyService.create(req).either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { id =>
      ZIO
        .attempt(id.toLong)
        .flatMap(companyService.getById)
        .catchSome { case _: NumberFormatException =>
          companyService.getBySlug(id)
        }
        .either
    }

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoints.serverLogic(_ => companyService.getAll().either)

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)
}

object CompanyController {
//  val makeZIO =
//    ZIO.service[CompanyService].map(companyService => new CompanyController(companyService))

  val makeZIO = for {
    service <- ZIO.service[CompanyService]
  } yield new CompanyController(service)
}
