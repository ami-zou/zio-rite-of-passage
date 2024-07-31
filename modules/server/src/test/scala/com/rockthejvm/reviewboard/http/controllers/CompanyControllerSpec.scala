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

object CompanyControllerSpec extends ZIOSpecDefault {
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO(endpointFunc: CompanyController => ServerEndpoint[Any, Task]) =
    for {
      controller <- CompanyController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointRunLogic(endpointFunc(controller))
          .backend()
      )
    } yield backendStub

  override def spec: Spec[TestEnvironment with Scope, Any] = ???
//    suite("CompanyControllerSpec")(
//      test("post company") {
//        //
//      }
//    )

}
