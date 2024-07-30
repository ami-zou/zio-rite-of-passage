package com.rockthejvm.reviewboard.domain.data

import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.Instant

final case class Review(
    id: Long, // PK
    companyId: Long,
    userId: Long, // FK
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    update: Instant
)

object Review {
  given codec: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}