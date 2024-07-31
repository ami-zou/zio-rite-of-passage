package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.services.*
import com.rockthejvm.reviewboard.syntax.*
import sttp.client3.*
import sttp.client3.testing.*
import sttp.monad.*
import sttp.tapir.EndpointIO.annotations.basic
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.*
import sttp.tapir.server.stub.*
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*
import zio.test.*

import java.time.Instant

object ReviewControllerSpec extends ZIOSpecDefault {
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private val goodReview = Review(
    id = 1L, // PK
    companyId = 1L,
    userId = 5, // FK
    management = 5,
    culture = 5,
    salary = 5,
    benefits = 5,
    wouldRecommend = 10,
    review = "all good",
    created = Instant.now(),
    update = Instant.now()
  )

  private val serviceStub = new ReviewService {
    override def create(request: CreateReviewRequest, userId: Long): Task[Review] =
      ZIO.succeed(goodReview)

    override def getById(id: Long): Task[Option[Review]] = ZIO.succeed {
      if (id == 1) Some(goodReview)
      else None
    }

    override def getByCompanyId(companyId: Long): Task[List[Review]] = ZIO.succeed {
      if (companyId == 1) List(goodReview)
      else List()
    }

    override def getByUserId(userId: Long): Task[List[Review]] = ZIO.succeed {
      if (userId == 1) List(goodReview)
      else List()
    }
  }

  private def backendStubZIO(endpointFunc: ReviewController => ServerEndpoint[Any, Task]) =
    for {
      controller <- ReviewController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointRunLogic(endpointFunc(controller))
          .backend()
      )
    } yield backendStub

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewControllerSpec")(
      test("post review") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
            .post(uri"reviews")
            .body(
              CreateReviewRequest(
                companyId = 1L,
                management = 5,
                culture = 5,
                salary = 5,
                benefits = 5,
                wouldRecommend = 10,
                review = "all good"
              ).toJson
            )
            .send(backendStub)
        } yield response.body

        program.assert(
          _.toOption // Option[String]
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview)
        )
      },
      test("get by id") {
        for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"reviews/1")
            .send(backendStub)
          responseNotFound <- basicRequest
            .get(uri"reviews/999")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview) &&
            responseNotFound.body.toOption.flatMap(_.fromJson[Review].toOption).isEmpty
        )
      },
      test("get by company id") {
        for {
          backendStub <- backendStubZIO(_.getByCompanyId)
          response <- basicRequest
            .get(uri"reviews/company/1")
            .send(backendStub)
          responseNotFound <- basicRequest
            .get(uri"reviews/company/999")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview)) &&
            responseNotFound.body.toOption
              .flatMap(_.fromJson[List[Review]].toOption)
              .contains(List())
        )
      }
    ).provide(ZLayer.succeed(serviceStub))
}
