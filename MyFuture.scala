package mycon

import scala.util.{Try, Success, Failure}

trait MyFuture[+T] {
  def onComplete[U](f: Try[T] => U, name: String = "default"): Unit
  def transform[S](f: Try[T] => Try[S], name: String = "default"): MyFuture[S]
  def transformWith[S](f: Try[T] => MyFuture[S], name: String = "default"): MyFuture[S]
  def map[S](f: T => S, name: String = "default"): MyFuture[S]
  def flatMap[S](f: T => MyFuture[S], name: String = "default"): MyFuture[S]
}

object MyFuture {
  def unit(name: String = "default"): MyFuture[Unit] = successful((), name)
  def failed[T](exception: Throwable, name: String = "default"): MyFuture[T] = MyPromise.failed(exception, name).future
  def successful[T](result: T, name: String = "default"): MyFuture[T] = MyPromise.successful(result, name).future
  def fromTry[T](result: Try[T], name: String = "default"): MyFuture[T] = MyPromise.fromTry(result, name).future
  def apply[T](body: =>T, name: String = "default"): MyFuture[T] = unit(name).map(_ => body, name)
}