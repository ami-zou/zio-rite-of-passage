package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.*
import com.rockthejvm.reviewboard.services.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.*
import zio.*
import zio.http.Server

object Application extends ZIOAppDefault {

  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default
      ).toHttp(endpoints)
    )
    _ <- Console.print("Server successfully started")
  } yield ()

  override def run =
    serverProgram.provide(
      Server.default,
      CompanyServiceLive.layer,
      CompanyRepositoryLive.layer,
      ReviewServiceLive.layer,
      ReviewRepositoryLive.layer,
      Repository.dataLayer
    )
}
