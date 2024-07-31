package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.services.{CompanyService, ReviewService}
import zio.*

class CompanyController private (companyService: CompanyService)
    extends BaseController
    with CompanyEndpoints {}

object CompanyController {
  val makeZIO =
    ZIO.service[CompanyService].map(companyService => new CompanyController(companyService))
}
