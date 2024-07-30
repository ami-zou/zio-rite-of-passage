package com.rockthejvm.reviewboard.repositories

import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

trait RepositorySpec {

  val initScript: String

  // test container
  // spawn a Postgres instance on Docker just for the test
  private def createContainer() = {
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres").withInitScript(initScript)
    container.start()
    container
  }

  private def closeContainer(container: PostgreSQLContainer[Nothing]): Unit = {
    container.close()
  }

  // create a Datasource to connect to the Postgres
  private def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val dataSource = new PGSimpleDataSource() // plain JDBC type
    dataSource.setURL(container.getJdbcUrl)
    dataSource.setUser(container.getUsername)
    dataSource.setPassword(container.getPassword)
    dataSource
  }

  // use the DataSource (as a ZLayer) to build Quill layer (as a ZLayer)
  val dataSourceLayer = ZLayer {
    for {
      container <- ZIO.acquireRelease(ZIO.attempt(createContainer()))(container =>
        ZIO.attempt(container.stop()).orDie
      )
      dataSource <- ZIO.attempt(createDataSource(container))
    } yield dataSource
  }

}
