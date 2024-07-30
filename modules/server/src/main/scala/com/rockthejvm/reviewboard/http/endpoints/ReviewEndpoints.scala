package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.EndpointIO.annotations.jsonbody
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait ReviewEndpoints {
  val createEndpoint =
    endpoint
      .tag("review")
      .name("create")
      .description("create a review of a company")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getByIdEndpoint =
    endpoint
      .tag("review")
      .name("getById")
      .description("get a review by ID")
      .in("reviews" / path[Long]("id"))
      .get
      .out(jsonBody[Option[Review]])

  val getByCompanyIdEndpoint =
    endpoint
      .tag("review")
      .name("getByCompanyId")
      .description("get a review by companyID")
      .in("reviews" / path[Long]("id"))
      .get
      .out(jsonBody[List[Review]])

  val getByUserIdEndpoint =
    endpoint
      .tag("review")
      .name("getByUserId")
      .description("get a review by userId")
      .in("reviews" / path[Long]("id"))
      .get
      .out(jsonBody[List[Review]])
}
