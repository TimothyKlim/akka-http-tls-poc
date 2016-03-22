import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream._
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.io.FileOutputStream

object Main extends App {
  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()

  import sys.dispatcher

  val src = "https://ucarecdn.com/2877682a-c715-4848-8fe2-4ff9fbd7728e/"

  // nio: Success
  val url = new URL(src)
  val rbc = Channels.newChannel(url.openStream())
  val fos = new FileOutputStream(Files.createTempFile(null, null).toFile)
  println(s"nio: ${fos.getChannel().transferFrom(rbc, 0, Long.MaxValue)}")

  // akka http: Failure(javax.net.ssl.SSLException: Received fatal alert: handshake_failure)
  Http()
    .singleRequest(HttpRequest(uri = src))
    .flatMap(_.entity.dataBytes.runFold(0L)(_ + _.length))
    .onComplete(res => println(s"akka-http: $res"))
}
