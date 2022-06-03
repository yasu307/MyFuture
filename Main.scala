import scala.util.{Try, Success, Failure}
import mycon.MyFuture

object Main{
  def main(args: Array[String]): Unit = {
    // val success = MyFuture.successful(1, "one")
    // success.onComplete(println, "")
    // success.onComplete(i => println(s"second on complete $i"))
    // success.onComplete(i => 123)

    // val failed = MyFuture.failed(new Exception("error!"))
    // failed.onComplete(println)
    // failed.onComplete(i => println(s"second on complete $i"))
    // failed.onComplete(i => 123)

    // // tranforsm, transformWithメソッド作成後
    // MyFuture.successful(3).transform(_ map (_ * 3)).onComplete(println)
    // MyFuture.successful(4).transformWith(i => MyFuture.successful(i.map(_ * 3))).onComplete(println)

    // // map, flatMap, applyメソッド作成後
    // MyFuture.successful(5).flatMap(i => MyFuture.successful(6).map(_ + i)).onComplete(println)

    // MyFuture(123).onComplete(println)
    // MyFuture(2/0).onComplete(println)
    MyFuture.successful(1, "one").flatMap(i => MyFuture.successful(2, "two").map((_ + i), "three"), "four").onComplete(println, "five")
  }
}