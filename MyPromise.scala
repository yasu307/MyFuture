package com.yusuke.myconcurrent

import scala.util.{Try, Success, Failure}

trait MyPromise[T] {
  def future: MyFuture[T]
  def tryComplete(result: Try[T]): Boolean
}

object MyPromise {
  def apply[T](): MyPromise[T] = new impl.MyPromise.DefaultPromise[T]()
  def failed[T](exception: Throwable): MyPromise[T] = fromTry(Failure(exception))
  def successful[T](result: T): MyPromise[T] = fromTry(Success(result))
  def fromTry[T](result: Try[T]): MyPromise[T] = {
    val myPromise = new impl.MyPromise.DefaultPromise[T]()
    myPromise.set(result)
    myPromise
  } 
}

