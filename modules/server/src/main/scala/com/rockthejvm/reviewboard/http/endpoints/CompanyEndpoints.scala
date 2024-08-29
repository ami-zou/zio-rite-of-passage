package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait CompanyEndpoints extends BaseEndpoint {
  val createEndpoint =
    baseEndpoint
      .tag("companies")
      .name("create")
      .description("create a listing of a company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoints =
    baseEndpoint
      .tag("companies")
      .name("getAll")
      .description("get all listings of a company")
      .in("companies")
      .get
      .out(jsonBody[List[Company]])

  val getByIdEndpoint =
    baseEndpoint
      .tag("companies")
      .name("getById")
      .description("get a listing of a company")
      .in("companies" / path[String]("id"))
      .get
      .out(jsonBody[Option[Company]])
}
