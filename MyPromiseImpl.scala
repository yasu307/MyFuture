package com.yusuke.myconcurrent.impl

import scala.util.{ Try, Success, Failure }
import java.util.concurrent.atomic.AtomicReference

trait MyPromise[T]
  extends com.yusuke.myconcurrent.MyFuture[T] with com.yusuke.myconcurrent.MyPromise[T] {
  def future = this
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
          l.value.compareAndSet(None, Some(result))
          // 新規スレッドを作成し実行
          new java.lang.Thread(l).start()
        }
        true
    }

    override def onComplete[U](f: Try[T] => U): Unit = {
      // 引数の変換を持つ新しいコールバックを生成
      val newRunnable = new CallbackRunnable[T](f)
      get match {
        // 処理が完了していたら新しく作成したコールバックを実行する
        case t: Try[T]            =>
          newRunnable.value.compareAndSet(None, Some(t))
          new java.lang.Thread(newRunnable).start()
        // 処理が完了していなければコールバックのリストに新しく生成したコールバックを加える
        case list: Seq[CallbackRunnable[T]] =>
          set(newRunnable +: list)
      }
    }
  }
}

class CallbackRunnable[T](f: Try[T] => Any) extends Runnable {
  val value: java.util.concurrent.atomic.AtomicReference[Option[Try[T]]] =
    new java.util.concurrent.atomic.AtomicReference(None)
  // valueに引数で受け取った関数を適用する　引数で受け取った関数はコールバックの代わり
  // このメソッドは別スレッドで実行される予定 Runnable.runメソッドをoverride
  // 戻り値の型がUnitでいいのか？ runの結果はどうやってコールバック宣言元に渡されるのか
  // このメソッドの結果（コールバックで渡された変換の結果）がFutureに渡されるわけではない
  override def run(): Unit =
    value.get.fold(sys.error("value must be set"))(v => f(v))
}
