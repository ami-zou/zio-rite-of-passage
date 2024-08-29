package com.rockthejvm.reviewboard.http.endpoints
import com.rockthejvm.reviewboard.domain.error.HttpError
import sttp.tapir.*

trait BaseEndpoint {
  val baseEndpoint = endpoint
    .errorOut(statusCode and plainBody[String])
    .mapErrorOut[Throwable](
      /* Takes in tuples (StatusCode, String) => function MyHttpError */ HttpError.decode
    )(HttpError.encode) // Conversion functions

}
