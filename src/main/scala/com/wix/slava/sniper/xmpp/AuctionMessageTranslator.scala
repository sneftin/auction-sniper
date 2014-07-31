package com.wix.slava.sniper.xmpp

import com.wix.slava.sniper.PriceSourceEnum.PriceSource
import com.wix.slava.sniper.{PriceSourceEnum, AuctionEventListener}

import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.{Chat, MessageListener}

trait XMPPFailureReporter {
  def cannotTranslateMessage(auctionId: String, failedMessage: String, exception: Exception)
}

class MissingValueException(fieldName: String) extends Exception("Missing value for field: " + fieldName) {}



class AuctionMessageTranslator(sniperId:String, private val listener:AuctionEventListener, failureReporter: XMPPFailureReporter) extends MessageListener {

  def processMessage(chat: Chat, message: Message) {
    val msgBody = message.getBody
    try {
      translateMessage(msgBody)
    }
    catch {
      case e:Exception => listener.auctionFailed; failureReporter.cannotTranslateMessage(sniperId, msgBody, e)
    }
  }

  def translateMessage(message:String) {
    val event = AuctionEvent.from(message)
    val eventType = event.eventType

    if (eventType=="CLOSE")
      listener.auctionClosed
    else if (eventType == "PRICE")
      listener.currentPrice(event.currentPrice, event.increment, event.isFrom(sniperId))
  }


  class AuctionEvent {
    val fields = scala.collection.mutable.Map[String,String]()

    def eventType = { get("Event") }
    def currentPrice = { getInt("CurrentPrice")}
    def increment = { getInt("Increment")}
    def bidder = { get("Bidder")}
    def isFrom(sniperId:String): PriceSource = {
      if (bidder == sniperId)
        return PriceSourceEnum.FromSniper
      else
        return PriceSourceEnum.FromOtherBidder
    }
    private def getInt(fieldName: String) = { get(fieldName).toInt }

    private def get(fieldName: String) = {
      val value = fields(fieldName)
      if (value == null)
        throw new MissingValueException(fieldName)
      value
    }
    def addField(field: String) {
      val e = field.split(":")
      fields += e(0).trim -> e(1).trim
    }
  }

  object AuctionEvent {

    def from(messageBody:String): AuctionEvent = {
      val event = new AuctionEvent
      messageBody.split(";").foreach(event.addField(_))
      /*for (part <- messageBody.split(";")) {
        event.addField(part)
      }*/
      return event
    }
  }
}
