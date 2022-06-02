package com.yusuke.myconcurrent

import scala.util.{Try, Success, Failure}

trait MyPromise[T] {
  // MyPromiseをMyFutureに変換するメソッド
  def future: MyFuture[T]
  // MyPromiseに値を設定し、それが成功したか否かを返す
  def tryComplete(result: Try[T]): Boolean
}

object MyPromise {
  // tryからMyPromiseを作成する
  def fromTry[T](result: Try[T]): MyPromise[T] = {
    // DefaultPromiseの生成
    val myPromise = new impl.MyPromise.DefaultPromise[T]()
    // DefaultPromiseの結果に引数で受け取った値を設定する
    myPromise.value = result
    // 結果をもつDefaultPromiseを返す
    myPromise
  }
  // 成功の結果をもつMyPromiseを作成する
  // fromTryの引数にSuccessを送っているだけ
  def successful[T](result: T): MyPromise[T] = fromTry(Success(result))

  // 失敗の結果をもつMyPromiseを作成する
  // fromTryの引数にFailureを送っているだけ
  def failed[T](exception: Throwable): MyPromise[T] = fromTry(Failure(exception))

  // 処理は簡単だけど使わないのでコメントアウト
  // def apply[T](): MyPromise[T] = new impl.MyPromise.DefaultPromise[T]()
}

