package com.yusuke.myconcurrent

import scala.util.{Try, Success, Failure}

trait MyPromise[T] {
  def future: MyFuture[T]
  def tryComplete(result: Try[T]): Boolean
}

object MyPromise {
  def apply[T](name: String = "default"): MyPromise[T] = new impl.MyPromise.DefaultPromise[T](name)
  def failed[T](exception: Throwable, name: String = "default"): MyPromise[T] = fromTry(Failure(exception), name)
  def successful[T](result: T, name: String = "default"): MyPromise[T] = fromTry(Success(result), name)
  def fromTry[T](result: Try[T], name: String = "default"): MyPromise[T] = {
    val myPromise = new impl.MyPromise.DefaultPromise[T](name)
    myPromise.set(result)
    myPromise
  } 
}

