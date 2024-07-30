package com.rockthejvm.reviewboard.services
import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import com.rockthejvm.reviewboard.services.*
import zio.*
import zio.test.*

import java.time.Instant

object ReviewServiceSpec extends ZIOSpecDefault {
  val goodReview = Review(
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

  val badReview = Review(
    id = 2L, // PK
    companyId = 1L,
    userId = 5, // FK
    management = 1,
    culture = 1,
    salary = 1,
    benefits = 1,
    wouldRecommend = 1,
    review = "terrible",
    created = Instant.now(),
    update = Instant.now()
  )

  val stubRepoLayer = ZLayer.succeed {
    new ReviewRepository {
      override def create(review: Review): Task[Review] = ZIO.succeed(goodReview)

      override def getById(id: Long): Task[Option[Review]] = ZIO.succeed {
        id match {
          case 1 => Some(goodReview)
          case 2 => Some(badReview)
          case _ => None
        }
      }

      override def getByCompanyId(id: Long): Task[List[Review]] = ZIO.succeed {
        if (id == 1) List(goodReview, badReview)
        else List()
      }

      override def getByUserId(id: Long): Task[List[Review]] = ZIO.succeed {
        if (id == 1) List(goodReview, badReview)
        else List()
      }

      override def update(id: Long, op: Review => Review): Task[Review] =
        getById(id).someOrFail(new RuntimeException(s"id $id not found")).map(op)

      override def delete(id: Long): Task[Review] =
        getById(id).someOrFail(new RuntimeException(s"id $id not found"))
    }
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewServiceSpec")(
      test("create") {
        for {
          service <- ZIO.service[ReviewService]
          review <- service.create(
            CreateReviewRequest(
              companyId = goodReview.companyId,
              management = goodReview.management,
              culture = goodReview.culture,
              salary = goodReview.salary,
              benefits = goodReview.benefits,
              wouldRecommend = goodReview.wouldRecommend,
              review = goodReview.review
            ),
            userId = 1L
          )
        } yield assertTrue(
          review.companyId == goodReview.companyId &&
            review.management == goodReview.management &&
            review.culture == goodReview.culture &&
            review.salary == goodReview.salary &&
            review.benefits == goodReview.benefits &&
            review.wouldRecommend == goodReview.wouldRecommend &&
            review.review == goodReview.review
        )
      },
      test("get By ID") {
        for {
          service        <- ZIO.service[ReviewService]
          review         <- service.getById(1L)
          reviewNotFound <- service.getById(999L)
        } yield assertTrue(
          review.contains(goodReview) && reviewNotFound.isEmpty
        )
      },
      test("get By Company") {
        for {
          service        <- ZIO.service[ReviewService]
          review         <- service.getByCompanyId(1L)
          reviewNotFound <- service.getByCompanyId(999L)
        } yield assertTrue(
          review.contains(goodReview) && reviewNotFound.isEmpty
        )
      },
      test("get By User") {
        for {
          service        <- ZIO.service[ReviewService]
          review         <- service.getByUserId(1L)
          reviewNotFound <- service.getByUserId(999L)
        } yield assertTrue(
          review.contains(goodReview) && reviewNotFound.isEmpty
        )
      }
    ).provide(
      ReviewServiceLive.layer,
      stubRepoLayer
    )
}
