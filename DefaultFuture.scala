import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

object DefaultFuture{
  Future.successful(123).onComplete(println)

  val future1 = Future.successful(123).transform(_ map (_ * 2))
  val future2 = Future.successful(123).transformWith(i => Future.successful(i.map(_ * 2)))

  Future.successful(1).flatMap(i => Future.successful(2).map(_ + i)).onComplete(println)
}