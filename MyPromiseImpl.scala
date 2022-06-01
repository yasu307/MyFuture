package com.yusuke.myconcurrent.impl

import scala.util.{ Try, Success, Failure }
import java.util.concurrent.atomic.AtomicReference

trait MyPromise[T]
  extends com.yusuke.myconcurrent.MyFuture[T] with com.yusuke.myconcurrent.MyPromise[T] {
  def future = this

  import com.yusuke.myconcurrent.MyFuture
  import com.yusuke.myconcurrent.impl.MyPromise.DefaultPromise

  override def transform[S](f: Try[T] => Try[S]): MyFuture[S] = {
    // 新しいMyFuturePromiseを作成
    val promise = new DefaultPromise[S]
    // このクラス(MyFuturePromise)にコールバックを追加する
    // 結果を引数で変換し、新しく作成したMyFuturePromiseの結果として格納する
    onComplete { result =>
      promise.tryComplete(f(result))
    }
    // MyFuturePromiseをMyFutureとして返す
    promise.future
  }
  override def transformWith[S](f: Try[T] => MyFuture[S]): MyFuture[S] = {
    val promise = new DefaultPromise[S]
    // 結果を引数で変換する。変換後のMyFutureに以下のコールバックを追加する
    // 結果を新しく作成したpromiseに格納する
    onComplete { r1 =>
      f(r1).onComplete { r2 =>
        promise.tryComplete(r2)
      }
    }
    // 新しく作成した
    promise.future
  }
}

object MyPromise                                                                          {
  class DefaultPromise[T] extends AtomicReference[AnyRef](Nil) with MyPromise[T] {
    override def tryComplete(result: Try[T]): Boolean = get match {
      // もし既に結果が格納されていれば何もせずfalseを返す
      case _: Try[_]       =>
        false
      // もしコールバックのリストが格納されていれば、格納し、さらにコールバックにも値をセットし、コールバックを実行する
      case list: Seq[CallbackRunnable[T]] =>
        list.foreach { l =>
          // コールバックの値に結果が設定されていなければ、値を設定する
          if(l.value == null){
            l.value = result
            new java.lang.Thread(l).start()
          }
        }
        true
    }

    override def onComplete[U](f: Try[T] => U): Unit = {
      // 引数の変換を持つ新しいコールバックを生成
      val newRunnable = new CallbackRunnable[T](f)
      get match {
        // 処理が完了していたら新しく作成したコールバックを実行する
        case t: Try[T]            =>
          newRunnable.value = t
          new java.lang.Thread(newRunnable).start()
        // 処理が完了していなければコールバックのリストに新しく生成したコールバックを加える
        case list: Seq[CallbackRunnable[T]] =>
          set(newRunnable +: list)
      }
    }
  }
}

class CallbackRunnable[T](f: Try[T] => Any) extends Runnable {
  var value: Try[T] = null
  // valueに引数で受け取った関数を適用する　引数で受け取った関数はコールバックの代わり
  // このメソッドは別スレッドで実行される予定 Runnable.runメソッドをoverride
  // 戻り値の型がUnitでいいのか？ runの結果はどうやってコールバック宣言元に渡されるのか
  // このメソッドの結果（コールバックで渡された変換の結果）がFutureに渡されるわけではない
  override def run(): Unit = {
    require(value ne null) // must set value to non-null before running!
    f(value)
  }
}
