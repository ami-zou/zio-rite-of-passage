package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.EndpointIO.annotations.jsonbody
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait CompanyEndpoints {
  val createEndpoint =
    endpoint
      .tag("companies")
      .name("create")
      .description("create a listing of a company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])
}
