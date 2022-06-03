package com.yusuke.myconcurrent.impl

import scala.util.{ Try, Success, Failure }
import java.util.concurrent.atomic.AtomicReference

trait MyPromise[T]
  extends com.yusuke.myconcurrent.MyFuture[T] with com.yusuke.myconcurrent.MyPromise[T] {
  // ただ本体を返しているだけ、MyFutureを継承しているのでMyFuture型として返すことができる
  // Promiseメソッドでも使われていた
  def future = this

  // transform, transformWithメソッドのためにインポート
  import com.yusuke.myconcurrent.MyFuture
  import com.yusuke.myconcurrent.impl.MyPromise.DefaultPromise
  
  override def transform[S](f: Try[T] => Try[S]): MyFuture[S] = {
    // 新しいDefaultPromiseを作成
    val promise = new DefaultPromise[S]
    // このクラス(DefaultPromise)にコールバックを追加する
    // コールバックの内容 : 結果を引数で変換し、新しく作成したDefaultPromiseの結果として格納する
    onComplete { result =>
      promise.tryComplete(f(result))
    }
    // 新しく作成したDefaultPromiseをMyFutureとして返す
    promise.future
  }

  override def transformWith[S](f: Try[T] => MyFuture[S]): MyFuture[S] = {
    // 新しいDefaultPromiseを作成
    val promise = new DefaultPromise[S]
    // このクラス(DefaultPromise)にコールバックを追加する
    // コールバックの内容 : 
    //   結果を引数で変換した結果新しいMyFutureが生成される
    //   新しいMyFutureにコールバックを追加する
    //     コールバックの内容 : 生成されたMyFutureの結果を新しいDefaultPromiseの結果として格納する 
    onComplete { r1 =>
      f(r1).onComplete { r2 =>
        promise.tryComplete(r2)
      }
    }
    // 新しく作成したDefaultPromiseをMyFutureとして返す
    promise.future
  }
}

object MyPromise{     
  class DefaultPromise[T] extends MyPromise[T] {
    var value: AnyRef = Nil

    // valueに値を格納するメソッド
    override def tryComplete(result: Try[T]): Boolean = value match {
      // もし既に結果が格納されていれば何もせずfalseを返す
      case _: Try[_]       =>
        false
      // もしコールバックのリストが格納されていれば、値を格納する
      // さらにコールバックの値に結果をセットし、コールバックを実行する
      case list: Seq[CallbackRunnable[T]] =>
        list.foreach { l =>
          if(l.value == null){
            l.value = result
            new java.lang.Thread(l).start()
          }
        }
        true
    }

    // コールバックを設定するメソッド
    override def onComplete[U](f: Try[T] => U): Unit = {
      // 引数の変換を持つ新しいコールバックを生成
      val newRunnable = new CallbackRunnable[T](f)
      value match {
        // 処理が完了していたら新しく作成したコールバックを実行する
        case t: Try[T]            =>
          newRunnable.value = t
          new java.lang.Thread(newRunnable).start()
        // 処理が完了していなければコールバックのリストに新しく生成したコールバックを加える
        case list: Seq[CallbackRunnable[T]] =>
          value = list :+ newRunnable
      }
    }
  }
}

// 一つのコールバックを表す
// 引数にコールバックとして実行する変換をとる
// 非同期に処理が可能
class CallbackRunnable[T](f: Try[T] => Any) extends Runnable {
  // DefaultPromiseの結果の処理が終了したら、その値が格納される
  // 最初はnullだがDefaultPromiseの結果の処理が終わったら代入される
  var value: Try[T] = null

  // Runnable.runを継承
  // java.lang.Threadから非同期に実行できる
  override def run(): Unit = {
    f(value)
  }
}
