package controllers

// (c) Copyright IBM Corporation. 2019
// SPDX-License-Identifier: Apache-2.0
// By Kip Twitchell

import java.nio.file.Files
import java.io.File
import java.nio.file.attribute.{BasicFileAttributeView, BasicFileAttributes}
import java.text.SimpleDateFormat
import java.util.Locale
import datatypes.FileAttributes
import play.api.mvc._
import play.api._
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}



/**
  * This controller handles a file list process.
  */
@Singleton
class ListFilesController @Inject() (cc:MessagesControllerComponents)
                               (implicit executionContext: ExecutionContext, assetsFinder: AssetsFinder)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)


  /**
    * Renders a list of files.
    */
  def listfiles = Action { implicit request =>
    Ok(views.html.listfiles("Universal Ledger List Files", getList))
  }

  def getList: List[FileAttributes] = {
    // Get list of files from the upload directory

    val formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.US)
    // Maintains milli seconds
    //    val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z", Locale.US)
    val parentDir = new File("/home/vagrant/uldata/")

    def getListOfFiles(dir: File): List[File] = {
      if (dir.exists && dir.isDirectory) {
        dir.listFiles.filter(_.isFile).toList
      } else {
        List[File]()
      }
    }

    val filelist: List[File] = getListOfFiles(parentDir)
//    println("list of files: ")
//    filelist.foreach(x => println("name: " + x.getName))

    def buildAttList(file: File): FileAttributes = {
      val view = Files.getFileAttributeView(file.toPath, classOf[BasicFileAttributeView])
      val attrs: BasicFileAttributes = view.readAttributes
      val fileattributes: FileAttributes = FileAttributes(
        file.getName,
        formatter.format(attrs.creationTime.toMillis),
        attrs.isDirectory,
        attrs.isOther,
        attrs.isRegularFile,
        attrs.isSymbolicLink,
        try {
          formatter.format(attrs.lastAccessTime)
        } catch {
          case e: Exception => "None"
        },
        try {
          formatter.format(attrs.lastModifiedTime)
        } catch {
          case e: Exception => "None"
        },
        attrs.size
      )
      fileattributes
    }

    // Need to filter for regular files here.

    val displaylist: List[FileAttributes] = {
      for {x <- filelist} yield buildAttList(x)
    }
    displaylist
  }

}

