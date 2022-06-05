import scala.util.{ Try, Success, Failure }
import com.yusuke.myconcurrent.MyFuture

object Main {
  def main(args: Array[String]): Unit = {
    // val fut: MyFuture[Int] = MyFuture.successful(1)
    // fut.onComplete {
    //   case Success(i) => println(i * 5)
    //   case Failure(e) => println(e.getMessage())
    // }

    // val failed: MyFuture[Int] = MyFuture.failed(new Exception("error!"))
    // failed.onComplete(println)
    // failed.onComplete{
    //   case Success(i) => println(i * 5)
    //   case Failure(e) => println(e.getMessage())
    // }

    // tranforsm, transformWithメソッド作成後
    // MyFuture.successful(3).transform(_ map (_ * 3))
    // MyFuture.successful(4).transformWith(i => MyFuture.successful(i.map(_ * 3)))

    // 稀に結果が表示されないバグがあります　詳しい発生原因は分かりません
    // 結果が表示される時もあるので別スレッドの処理が終わる前にメインスレッドが終了してしまっていると思っていたのですが
    // Thread.sleepしても改善されないため何かしら別の原因があるのかもしれません。
    // 原因を突き止めようと内部処理の中でprintやloggerを使用すると、結果が表示されてしまうため、原因がわからない状態です
    MyFuture.successful(1).flatMap(i => MyFuture.successful(2).map(_ + i)).onComplete(println)
  }
}
