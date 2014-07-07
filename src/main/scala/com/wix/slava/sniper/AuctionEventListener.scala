package com.wix.slava.sniper

import java.util.EventListener

import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.{Chat, MessageListener}


object PriceSourceEnum extends Enumeration {
  type PriceSource = Value
  val FromSniper = Value
  val FromOtherBidder = Value
}

import PriceSourceEnum._

trait AuctionEventListener extends EventListener {
  def auctionClosed
  def auctionFailed
  def currentPrice(price: Int, increment: Int, priceSource:PriceSource)
}
