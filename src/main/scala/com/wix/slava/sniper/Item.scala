package com.wix.slava.sniper

/**
 * Created by Slava_Neftin on 7/6/14.
 */
case class Item (id: String, stopPrice:Int) {
  def allowsBid(bid: Int) = bid<=stopPrice
}
