package controllers
// (c) Copyright IBM Corporation. 2019
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell

import java.io.File
import java.nio.file.{Files, Path, Paths}
import javax.inject._

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo

import scala.concurrent.{ExecutionContext, Future}

case class FormData(name: String)

/**
  * This controller handles a file upload.
  */
@Singleton
class UploadController @Inject() (cc:MessagesControllerComponents)
                               (implicit executionContext: ExecutionContext, assetsFinder: AssetsFinder)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  val uploadtargetdir="/home/vagrant/uldata/"

  val form = Form(
    mapping(
      "name" -> text
    )(FormData.apply)(FormData.unapply)
  )

  println("entering")

  /**
    * Renders a start page.
    */
  def upload = Action { implicit request =>
    Ok(views.html.upload("Uninversal Ledger Upload", form))
  }

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  /**
    * Uses a custom FilePartHandler to return a type of "File" rather than
    * using Play's TemporaryFile class.  Deletion must happen explicitly on
    * completion, rather than TemporaryFile (which uses finalization to
    * delete temporary files).
    *
    * @return
    */
  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType, _) =>
      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          logger.info(s"count = $count, status = $status")
          println(s"count = $count, status = $status")
          FilePart(partName, filename, contentType, path.toFile)
      }
  }

  /**
    * A generic operation on the temporary file that deletes the temp file after completion.
    */
  private def operateOnTempFile(file: File, filename: String) = {
    val size = Files.size(file.toPath)
    logger.info(s"size = ${size}")
//    println("dir test")
    //make directory if needed
//    val uploadtargetdir = "/home/vagrant/uldata/"
    val path: Path = Paths.get(uploadtargetdir)
    var dircreated = "N"
    if (!Files.exists(path)) {
      Files.createDirectory(path)
      println("Directory created: " + uploadtargetdir)
      logger.info("Directory created: " + uploadtargetdir)
      dircreated = "Y"
    }

    else     {
      logger.info("Directory already exists: " + uploadtargetdir)
      println("Directory already exists: " + uploadtargetdir)
    }
    val movedFile = new File(uploadtargetdir, filename)
    file.renameTo(movedFile)
    logger.info("target file =" + movedFile)
    println("target file =" + movedFile)
    //    Files.deleteIfExists(file.toPath)
    size
  }

  /**
    * Uploads a multipart file as a POST request.
    *
    * @return
    */
  def uploadform = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
//    println("upload form")
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
        println(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
        val data = operateOnTempFile(file, filename)
        data

    }

//    Ok(routes.ListFilesController.listfiles)


      Redirect("/listfiles")

//        Ok(s"file size = ${fileOption.getOrElse("no file")}")
  }

}
