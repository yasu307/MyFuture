import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

// 通常のFutureを使うobject
// このobjectは実行されない
// メソッドの使い方を説明したり、Futureの実装へジャンプするために使う
object DefaultFuture{
  // onComplete
  // 一つのFutureに複数のコールバックを設定することが可能
  val future = Future.successful(123)
  future.onComplete(println)
  future.onComplete(println)

  // transform, transformWith
  val future1 = Future.successful(123).transform(_ map (_ * 2))
  val future2 = Future.successful(123).transformWith(i => Future.successful(i.map(_ * 2)))

  // map, flatMap
  Future.successful(1).flatMap(i => Future.successful(2).map(_ + i)).onComplete(println)
}