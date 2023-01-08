import cats.Parallel
import cats.effect.unsafe.implicits.global
import cats.effect.{Async, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.ConnectionFailure
import telegramium.bots.high._

import scala.concurrent.duration._
import scala.util.control.NonFatal


object Main extends IOApp.Simple {
  val app = new App[IO]
  val run = IO.pure(app.app).unsafeRunSync().handleErrorWith{
    case ConnectionFailure(t) =>
      t._3.printStackTrace()
      IO.sleep(5.seconds).flatMap(_ => run)
    case NonFatal(t) =>
      println("IO handle error with")
      t.printStackTrace()
      println("IO handle error with")
      IO.unit
  }
}

class App[F[_]: Async: Parallel]{
  val token = "5830377287:AAEMDacx2m2_nCr4dRDxFWTaHLxjW-sWPzM"
  def app = BlazeClientBuilder[F].resource.use { httpClient =>
    println("Hello I started")
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = s"https://api.telegram.org/bot$token")
    val bot = new MyLongPollBot()
    bot.start()
  }
}

