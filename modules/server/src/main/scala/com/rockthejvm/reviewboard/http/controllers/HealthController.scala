package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoint {
  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("Service is healthy"))
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}