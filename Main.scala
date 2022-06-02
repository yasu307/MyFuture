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

    val failed: MyFuture[Int] = MyFuture.failed(new Exception("error!"))
    failed.onComplete(println)
    failed.onComplete{
      case Success(i) => println(i * 5)
      case Failure(e) => println(e.getMessage())
    }

    // tranforsm, transformWithメソッド作成後
    MyFuture.successful(3).transform(_ map (_ * 3)).onComplete(println)
    MyFuture.successful(4).transformWith(i => MyFuture.successful(i.map(_ * 3))).onComplete(println)
  }
}
