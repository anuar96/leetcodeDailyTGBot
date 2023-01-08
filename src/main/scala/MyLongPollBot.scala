import cats.effect.Async
import cats.effect.kernel.Sync
import cats.implicits.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import cats.{Applicative, Parallel}
import io.circe._
import io.circe.parser._
import sttp.client3.quick._
import telegramium.bots.high.implicits._
import telegramium.bots.high.{Api, LongPollBot, Methods}
import telegramium.bots.{ChatIntId, Message}

import scala.util.Try

class MyLongPollBot[F[_] : Async : Parallel : Applicative]()(implicit api: Api[F]) extends LongPollBot[F](api) {

  override def onMessage(msg: Message): F[Unit] = msg.text.getOrElse("empty").toLowerCase match {
    case "give" | "daily" | "challange" | "daily challange" =>
      Sync[F].handleErrorWith(dailyChallange(msg)) { e =>
        e.printStackTrace()
        e.raiseError
      }

    case "random easy" => printToTgChat("todo", msg)
    case "random medium" => printToTgChat("todo", msg)
    case _ => printToTgChat("a leetcode a day keeps unemployment away!", msg)
  }

  private def dailyChallange(msg: Message) = {
    println("request for daily came")
    for {
      body <- Async[F].fromTry(Try(
        quickRequest.post(uri"https://leetcode.com/graphql")
          .header("Content-Type", "application/json") //TODO потраить тут throw new Excpetion
          .body("{\"query\":\"query questionOfToday {\\n\\tactiveDailyCodingChallengeQuestion {\\n\\t\\tdate\\n\\t\\tuserStatus\\n\\t\\tlink\\n\\t\\tquestion {\\n\\t\\t\\tacRate\\n\\t\\t\\tdifficulty\\n\\t\\t\\tfreqBar\\n\\t\\t\\tfrontendQuestionId: questionFrontendId\\n\\t\\t\\tisFavor\\n\\t\\t\\tpaidOnly: isPaidOnly\\n\\t\\t\\tstatus\\n\\t\\t\\ttitle\\n\\t\\t\\ttitleSlug\\n\\t\\t\\thasVideoSolution\\n\\t\\t\\thasSolution\\n\\t\\t\\ttopicTags {\\n\\t\\t\\t\\tname\\n\\t\\t\\t\\tid\\n\\t\\t\\t\\tslug\\n\\t\\t\\t}\\n\\t\\t}\\n\\t}\\n}\\n\",\"operationName\":\"questionOfToday\"}")
          .send(backend).body)
      )
      b = parse(body).getOrElse(Json.Null)
      //        link = root.data.activeDailyCodingChallengeQuestion.link.string.getOption(b).getOrElse(throw new Exception(s"can't parse json $b"))
      //        difficulty = root.data.activeDailyCodingChallengeQuestion.difficulty.string.getOption(b).getOrElse(throw new Exception(s"can't parse json $b"))

      link2 <- b.hcursor.downField("data")
        .downField("activeDailyCodingChallengeQuestion")
        .get[String]("link")
        .toTry.toEither match {
        case Right(v) => v.pure
        case Left(e) => e.raiseError
      }

      difficulty2 <- b.hcursor.downField("data")
        .downField("activeDailyCodingChallengeQuestion")
        .downField("question")
        .get[String]("difficulty")
        .toTry.toEither match {
        case Right(v) => v.pure
        case Left(e) => e.raiseError
      }

      message = s"$difficulty2 https://leetcode.com/$link2"

      _ <- printToTgChat("HI", msg)
      r <- printToTgChat(message, msg)
    } yield r
  }


  private def printToTgChat(text: String, msg: Message) = {
    Methods.sendMessage(chatId = ChatIntId(msg.chat.id), text = text).exec.void
  }
}
/*

curl --request POST \
--url https://leetcode.com/graphql \
  --header 'Content-Type: application/json' \
--data '{"query":"query questionOfToday {\n\tactiveDailyCodingChallengeQuestion {\n\t\tdate\n\t\tuserStatus\n\t\tlink\n\t\tquestion {\n\t\t\tacRate\n\t\t\tdifficulty\n\t\t\tfreqBar\n\t\t\tfrontendQuestionId: questionFrontendId\n\t\t\tisFavor\n\t\t\tpaidOnly: isPaidOnly\n\t\t\tstatus\n\t\t\ttitle\n\t\t\ttitleSlug\n\t\t\thasVideoSolution\n\t\t\thasSolution\n\t\t\ttopicTags {\n\t\t\t\tname\n\t\t\t\tid\n\t\t\t\tslug\n\t\t\t}\n\t\t}\n\t}\n}\n","operationName":"questionOfToday"}'
*/
