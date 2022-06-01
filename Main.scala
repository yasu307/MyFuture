import scala.util.{Try, Success, Failure}
import com.yusuke.myconcurrent.MyFuture

object Main{
  def main(args: Array[String]): Unit = {
    MyFuture.successful(1).onComplete(println)
  }
}