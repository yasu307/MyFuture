package com.yusuke.myconcurrent

import scala.util.{Try, Success, Failure}

trait MyFuture[+T] {
  def onComplete[U](f: Try[T] => U): Unit
  def transform[S](f: Try[T] => Try[S]): MyFuture[S]
  def transformWith[S](f: Try[T] => MyFuture[S]): MyFuture[S]
    // map-flatMapから追加
  // 本来はここにコードが書いてあるが、見づらいので移動する
  def map[S](f: T => S): MyFuture[S]
  def flatMap[S](f: T => MyFuture[S]): MyFuture[S]
}

object MyFuture {
  // TryからMyFutureを作成するメソッド
  def fromTry[T](result: Try[T]): MyFuture[T] = MyPromise.fromTry(result).future
  // 成功の結果をもつFutureを作成するメソッド
  def successful[T](result: T): MyFuture[T] = MyPromise.successful(result).future
  // 失敗の結果をもつFUtureを作成するメソッド
  def failed[T](exception: Throwable): MyFuture[T] = MyPromise.failed(exception).future
  
  // applyはややこしいので省略 この説明を聞いた後なら理解できると思うので実際のコードを確かめてほしい
}