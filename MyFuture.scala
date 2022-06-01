package com.yusuke.myconcurrent

import scala.util.{Try, Success, Failure}

trait MyFuture[+T] {
  def onComplete[U](f: Try[T] => U): Unit
  def transform[S](f: Try[T] => Try[S]): MyFuture[S]
  def transformWith[S](f: Try[T] => MyFuture[S]): MyFuture[S]
  def map[S](f: T => S): MyFuture[S] = transform(_ map f)
  def flatMap[S](f: T => MyFuture[S]): MyFuture[S] = transformWith {
    case Success(s) => f(s)
    case Failure(_) => this.asInstanceOf[MyFuture[S]]
  }
}

object MyFuture {
  val unit: MyFuture[Unit] = successful(())
  def failed[T](exception: Throwable): MyFuture[T] = MyPromise.failed(exception).future
  def successful[T](result: T): MyFuture[T] = MyPromise.successful(result).future
  def fromTry[T](result: Try[T]): MyFuture[T] = MyPromise.fromTry(result).future
  def apply[T](body: =>T): MyFuture[T] = unit.map(_ => body)
}