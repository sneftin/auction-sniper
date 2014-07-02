package com.wix.slava.sniper

import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.{Chat, MessageListener}


object PriceSourceEnum extends Enumeration {
  type PriceSource = Value
  val FromSniper = Value
  val FromOtherBidder = Value
}

import PriceSourceEnum._

trait AuctionEventListener {
  def auctionClosed
  def currentPrice(price: Int, increment: Int, priceSource:PriceSource)
}

class AuctionMessageTranslator(sniperId:String, private val listener:AuctionEventListener) extends MessageListener {

  def processMessage(chat: Chat, message: Message) {
    val event = AuctionEvent.from(message.getBody())
    val eventType = event.eventType

    if (eventType=="CLOSE")
      listener.auctionClosed
    else if (eventType == "PRICE")
      listener.currentPrice(event.currentPrice, event.increment, event.isFrom(sniperId))
  }

  class AuctionEvent {
    val fields = scala.collection.mutable.Map[String,String]()

    def eventType = { fields("Event") }
    def currentPrice = { getInt("CurrentPrice")}
    def increment = { getInt("Increment")}
    def bidder = { fields("Bidder")}
    def isFrom(sniperId:String): PriceSource = {
      if (bidder == sniperId)
        return FromSniper
      else
        return FromOtherBidder
    }
    private def getInt(fieldName:String) = { fields(fieldName).toInt }

    def addField(field:String) {
      val e = field.split(":")
      fields += e(0).trim -> e(1).trim
    }
  }

  object AuctionEvent {

    def from(messageBody:String): AuctionEvent = {
      val event = new AuctionEvent
      for (part <- messageBody.split(";")) {
        event.addField(part)
      }
      return event
    }
  }
}
