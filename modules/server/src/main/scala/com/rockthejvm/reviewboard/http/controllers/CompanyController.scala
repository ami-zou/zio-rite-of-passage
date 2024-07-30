package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.services.{CompanyService, ReviewService}
import zio.*

class CompanyController private (companyService: CompanyService) {}

object CompanyController {
  val makeZIO =
    ZIO.service[CompanyService].map(companyService => new CompanyController(companyService))
}
