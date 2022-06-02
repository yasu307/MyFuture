package com.yusuke.myconcurrent.impl

import scala.util.{ Try, Success, Failure }
import java.util.concurrent.atomic.AtomicReference
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Future

trait MyPromise[T]
  extends com.yusuke.myconcurrent.MyFuture[T] with com.yusuke.myconcurrent.MyPromise[T] {
  def future = this

  import com.yusuke.myconcurrent.impl.MyPromise.DefaultPromise
}

object MyPromise extends LazyLogging {
  import com.yusuke.myconcurrent.MyFuture

  class DefaultPromise[T](val name: String = "default") extends AtomicReference[AnyRef](Nil) with MyPromise[T] {
    logger.debug(s"$name's DefaultPromise is created")
    override def tryComplete(result: Try[T]): Boolean = {
      logger.debug(s"$name 's tryComplete")
      get match {
        // もし既に結果が格納されていれば何もせずfalseを返す
        case _: Try[_]                      =>
          false
        // もしコールバックのリストが格納されていれば、格納し、さらにコールバックにも値をセットし、コールバックを実行する
        case list: Seq[CallbackRunnable[T]] =>
          list.foreach { l =>
            // コールバックの値に結果が設定されていなければ、値を設定する
            if (l.value == null) {
              l.value = result
              new java.lang.Thread(l).start()
            }
          }
          true
      }
    }

    override def onComplete[U](f: Try[T] => U, name: String = "default"): Unit = {
      logger.debug(s"${this.name} 's onComplete. $name 's function is passed")
      // 引数の変換を持つ新しいコールバックを生成
      val newRunnable = new CallbackRunnable[T](f, name)
      get match {
        // 処理が完了していたら新しく作成したコールバックを実行する
        case t: Try[T]                      =>
          logger.debug(s"${this.name} has finished. so $name 's callback is executed")
          newRunnable.value = t
          new java.lang.Thread(newRunnable).start()
        // 処理が完了していなければコールバックのリストに新しく生成したコールバックを加える
        case list: Seq[CallbackRunnable[T]] =>
          logger.debug(s"${this.name} hasn't finished. so $name 's callback is added to ${this.name} 's callbacklist")
          set(newRunnable +: list)
      }
    }

    override def transform[S](f: Try[T] => Try[S], name: String = "default"): MyFuture[S] = {
      logger.debug(s"${this.name} 's transform. $name 's function is passed")
      // 新しいMyFuturePromiseを作成
      val promise = new DefaultPromise[S](name)
      // このクラス(MyFuturePromise)にコールバックを追加する
      // 結果を引数で変換し、新しく作成したMyFuturePromiseの結果として格納する
      onComplete(
        { result =>
          promise.tryComplete(f(result))
        },
        s"${this.name}'s-callback-propagation-for-$name"
      )
      // MyFuturePromiseをMyFutureとして返す
      promise.future
    }
    override def transformWith[S](f: Try[T] => MyFuture[S], name: String = "default"): MyFuture[S] = {
      logger.debug(s"${this.name} 's transformWith. $name 's function is passed")
      val promise = new DefaultPromise[S](name)
      // 結果を引数で変換する。変換後のMyFutureに以下のコールバックを追加する
      // 結果を新しく作成したpromiseに格納する
      onComplete(
        { r1 =>
          f(r1).onComplete(
            { r2 =>
              promise.tryComplete(r2)
            },
            s"${this.name}'s-inner-callback-propagation-for-$name"
          )
        },
        s"${this.name}'s-outer-callback-propagation-for-$name"
      )
      // 新しく作成した
      promise.future
    }

    override def map[S](f: T => S, name: String = "default"): MyFuture[S] = {
      logger.debug(s"${this.name} 's map. $name 's function is passed")
      transform((_ map f), name)
    }

    override def flatMap[S](f: T => MyFuture[S], name: String = "default"): MyFuture[S] = {
      logger.debug(s"${this.name} 's flatMap. $name 's function is passed")
      transformWith(
        {
          case Success(s) => f(s)
          case Failure(_) => this.asInstanceOf[MyFuture[S]]
        },
        name
      )
    }

    override def toString: String = get match {
      case Some(result) => "Future("+result+")"
      case None => "Future(<not completed>)"
    }
  }
}

class CallbackRunnable[T](f: Try[T] => Any, name: String = "default") extends Runnable with LazyLogging {
  logger.debug(s"$name 's callbackrunnable is created")
  var value: Try[T] = null
  // valueに引数で受け取った関数を適用する　引数で受け取った関数はコールバックの代わり
  // このメソッドは別スレッドで実行される予定 Runnable.runメソッドをoverride
  // 戻り値の型がUnitでいいのか？ runの結果はどうやってコールバック宣言元に渡されるのか
  // このメソッドの結果（コールバックで渡された変換の結果）がFutureに渡されるわけではない
  override def run(): Unit = {
    logger.debug(s"$name 's callbackrunnable start")
    require(value ne null) // must set value to non-null before running!
    f(value)
    logger.debug(s"$name 's callbackrunnable end")
  }
}
