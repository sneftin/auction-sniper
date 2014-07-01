package com.wix.slava.sniper

import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.{Chat, MessageListener}


trait AuctionEventListener {
  def auctionClosed
  def currentPrice(price: Int, increment: Int)
}



class AuctionMessageTranslator(private val listener:AuctionEventListener) extends MessageListener {

  def processMessage(chat: Chat, message: Message) {
    val event = unpackEventFrom(message)
    val eventType = event("Event")

    if (eventType=="CLOSE")
      listener.auctionClosed
    else if (eventType == "PRICE")
      listener.currentPrice(event("CurrentPrice").toInt, event("Increment").toInt)
  }

  def unpackEventFrom(message:Message) : scala.collection.mutable.Map[String,String] = {
    val event = scala.collection.mutable.Map[String,String]()

    for (part <- message.getBody.split(";")) {
      val e = part.split(":")
      event += e(0).trim -> e(1).trim
    }
    return event
  }
}
