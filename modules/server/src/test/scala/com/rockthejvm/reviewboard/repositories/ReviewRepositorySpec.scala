package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.syntax.*
import io.getquill.autoQuote
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO, ZLayer}

import java.time.Instant

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec {
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

  override val initScript: String = "sql/reviews.sql"

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewRepositorySpec")(
      test("create review") {
        val program = for {
          repo   <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
        } yield review

        program.assert { review =>
          review.review == goodReview.review &&
          review.management == goodReview.management &&
          review.culture == goodReview.culture &&
          review.salary == goodReview.salary &&
          review.benefits == goodReview.benefits &&
          review.wouldRecommend == goodReview.wouldRecommend
        }
      },
      test("get review by ids (id, companyId, userId)") {
        for {
          repo   <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)

          fetchedReview  <- repo.getById(review.id)
          fetchedReview2 <- repo.getById(review.companyId)
          fetchedReview3 <- repo.getById(review.userId)
        } yield assertTrue(
          fetchedReview.contains(review) &&
            fetchedReview2.contains(review) &&
            fetchedReview3.contains(review)
        )
      },
      test("get all") {
        for {
          repo    <- ZIO.service[ReviewRepository]
          review  <- repo.create(goodReview)
          review2 <- repo.create(badReview)

          reviewsCompany <- repo.getByCompanyId(review.companyId)
          reviewsUser    <- repo.getByCompanyId(review.userId)
        } yield assertTrue(
          reviewsCompany.toSet == Set(review, review2) &&
            reviewsUser.toSet == Set(review, review2)
        )
      },
      test("edit review") {
        for {
          repo    <- ZIO.service[ReviewRepository]
          review  <- repo.create(goodReview)
          updated <- repo.update(review.id, _.copy(review = "not too bad"))
        } yield assertTrue(
          updated.review == "not too bad" &&
            review.id == updated.id &&
            review.companyId == updated.companyId &&
            review.management == updated.management &&
            review.created == updated.created &&
            review.update != updated.update
        )
      },
      test("delete review") {
        for {
          repo        <- ZIO.service[ReviewRepository]
          review      <- repo.create(goodReview)
          _           <- repo.delete(review.id)
          maybeReview <- repo.getById(review.id)
        } yield assertTrue(
          maybeReview.isEmpty
        )
      }
    ).provide(
      ReviewRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )

}
