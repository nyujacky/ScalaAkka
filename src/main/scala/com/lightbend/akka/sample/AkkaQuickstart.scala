package com.lightbend.akka.sample

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import scala.io.StdIn


object AkkaQuickstart extends App {

  import akka.stream.IOResult
  import akka.stream.alpakka.ftp.impl.{FtpLike, FtpSourceFactory, FtpSourceParams, SftpSourceParams}
  import akka.stream.alpakka.ftp.{FtpFile, RemoteFileSettings}
  import akka.stream.alpakka.ftp.SftpSettings

  import akka.stream.alpakka.ftp.scaladsl.Ftp
  import akka.stream.alpakka.ftp.scaladsl.Sftp
  import akka.stream.alpakka.ftp.RemoteFileSettings.FtpSettings
  import akka.stream.alpakka.ftp.FtpCredentials.AnonFtpCredentials
  import akka.stream.scaladsl._
  import akka.util.ByteString
  import akka.NotUsed
  import org.apache.commons.net.ftp.{FTP,FTPClient}
  import scala.concurrent.Future
  import java.net.InetAddress



  val system: ActorSystem = ActorSystem("helloAkka")

  val fileList = io.Source.fromFile("config.txt")
  // val fileList = io.Source.fromFile("config.txt").getLines.toList
  var chooseFileSource = ""
  var fileDirection = ""
  var host = ""
  var remoteFTPSiteDownload = ""
  var remoteSFTPSiteDownload = ""
  var remoteFTPSiteUpload = ""
  var remoteSFTPSiteUpload = ""

  val ftpSettings = new FtpSettings(
    InetAddress.getByName("localhost"),
    port = 1,
    AnonFtpCredentials,
    binary = true,
    passiveMode = true
  )
  val sftpSettings = new SftpSettings(
    InetAddress.getByName("localhost"),
    port = 1,
    AnonFtpCredentials,
    strictHostKeyChecking = false,
    knownHosts = None,
    sftpIdentity = None
  )
  def configReader(){
    fileList.getLines().foreach(line =>
      if (line.startsWith("1")){
        host = line.drop(17)
      }
      else if (line.startsWith("2")){
        remoteFTPSiteDownload = line.drop(20)
      }
      else if (line.startsWith("3")){
        remoteSFTPSiteDownload = line.drop(21)
      }
      else if (line.startsWith("4")){
        remoteSFTPSiteUpload = line.drop(18)
      }
      else if (line.startsWith("5")){
        remoteSFTPSiteUpload = line.drop(19)
      }

    )
  }
  def methodChooser(){
    if (fileDirection == "Read"){
      if(chooseFileSource == "FTP"){
        println(listFtpFiles(remoteFTPSiteDownload))
        println(retrieveFromPathFTP(remoteFTPSiteDownload))
      }
      else if(chooseFileSource == "SFTP"){
        println(listSftpFiles(remoteSFTPSiteDownload))
        println(retrieveFromPathFTP(remoteSFTPSiteDownload))
      }
    }
    if (fileDirection == "Write"){
      if(chooseFileSource == "FTP"){
        println(storeToPathFTP(remoteFTPSiteUpload, true))
      }
      else if(chooseFileSource == "SFTP"){
        println(storeToPathFTP(remoteSFTPSiteUpload, true))
      }
    }
  }

  protected def listFtpFiles(basePath: String): Source[FtpFile, NotUsed] =
  Ftp.ls(basePath, ftpSettings)

  protected def retrieveFromPathFTP(path: String): Source[ByteString, Future[IOResult]] =
  Ftp.fromPath(path, ftpSettings)

  protected def storeToPathFTP(path: String, append: Boolean): Sink[ByteString, Future[IOResult]] =
  Ftp.toPath(path, ftpSettings, append)

  protected def listSftpFiles(basePath: String): Source[FtpFile, NotUsed] =
  Sftp.ls(basePath, sftpSettings)

  protected def retrieveFromPathSFTP(path: String): Source[ByteString, Future[IOResult]] =
  Sftp.fromPath(path, sftpSettings)

  protected def storeToPathSFTP(path: String, append: Boolean): Sink[ByteString, Future[IOResult]] =
  Sftp.toPath(path, sftpSettings, append)

  try {
    configReader()

    println("\nWelcome to FTP/SFTP uploading/downloading. \n")
    println("Read or Write?")
    fileDirection = StdIn.readLine()

    println("\nFTP or SFTP?")
    chooseFileSource = StdIn.readLine()

    println("\nStarting FTP/SFTP downloading/uploading...\n")
    methodChooser()
    println("\nFinished Download/Uploading!")

    println(">>> Press ENTER to exit <<<")

    StdIn.readLine()
  } finally {
    system.terminate()
  }
}
