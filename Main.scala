import scala.util.{ Try, Success, Failure }
import com.yusuke.myconcurrent.MyFuture

object Main {
  def main(args: Array[String]): Unit = {
    val success: MyFuture[Int] = MyFuture.successful(1)
    success.onComplete(println)
    success.onComplete {
      case Success(i) => println(i * 5)
      case Failure(e) => println(e.getMessage())
    }

    // Awaitが使えないのでメインスレッドを停止して、MyFutureの処理を待つ
    Thread.sleep(3000)

    val failed: MyFuture[Int] = MyFuture.failed(new Exception("error!"))
    failed.onComplete(println)
    failed.onComplete{
      case Success(i) => println(i * 5)
      case Failure(e) => println(e.getMessage())
    }

    // Awaitが使えないのでメインスレッドを停止して、MyFutureの処理を待つ
    Thread.sleep(3000)

    // tranforsm, transformWithメソッド作成後
    val fut = MyFuture.successful(3).transform(_ map (_ * 2))
    fut.onComplete(println)
    val fut2 = MyFuture.successful(4).transformWith(i => MyFuture.successful(i.map(_ * 2)))
    fut2.onComplete(println)

    // Awaitが使えないのでメインスレッドを停止して、MyFutureの処理を待つ
    Thread.sleep(3000)
  }
}
