package com.wix.slava.sniper.unit

import com.wix.slava.sniper.PriceSourceEnum._
import com.wix.slava.sniper.xmpp.{XMPPFailureReporter, AuctionMessageTranslator}
import com.wix.slava.sniper.{AuctionEventListener, Main}
import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Message
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class AuctionMessageTranslatorTest extends Specification with Mockito {

  trait Context extends Scope {
    var UNUSED_CHAT: Chat = null
    val mockAuctionEventListener = mock[AuctionEventListener]
    val mockFailureReporter = mock[XMPPFailureReporter]
    val translator = new AuctionMessageTranslator(Main.SNIPER_ID, mockAuctionEventListener, mockFailureReporter)
  }
  "AuctionMessageTranslator" should {
    "notify auction closed when close message received" in new Context {
      val message = new Message()
      message.setBody("SQLVersion: 1.1; Event: CLOSE;")
      translator.processMessage(UNUSED_CHAT, message)
      there was one(mockAuctionEventListener).auctionClosed
    }
  }

  "AuctionMessageTranslator" should {
    "notify bid details when current price message received from other bidder" in new Context {
      val message = new Message()
      message.setBody("SQLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")
      translator.processMessage(UNUSED_CHAT, message)
      there was no(mockAuctionEventListener).auctionClosed
      there was one(mockAuctionEventListener).currentPrice(192,7,FromOtherBidder)
    }
  }

  "AuctionMessageTranslator" should {
    "notify bid details when current price message received from sniper" in new Context {
      val message = new Message()
      message.setBody("SQLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: " + Main.SNIPER_ID + ";")
      translator.processMessage(UNUSED_CHAT, message)
      there was no(mockAuctionEventListener).auctionClosed
      there was one(mockAuctionEventListener).currentPrice(192,7,FromSniper)
    }
  }

  "AuctionMessageTranslator" should {
    "notify auction failed when bad message arrives" in new Context {
      val msgStr = "a bad message"
      val message = new Message()
      message.setBody(msgStr)
      translator.processMessage(UNUSED_CHAT, message)
      there was one(mockAuctionEventListener).auctionFailed
      //TODO: this check is not working. why?
      //there was one(mockFailureReporter).cannotTranslateMessage(Main.SNIPER_ID, msgStr, any[Exception])
    }
  }

  "AuctionMessageTranslator" should {
    "notify auction failed when event type missing" in new Context {
      val message = new Message()
      message.setBody("SQLVersion: 1.1; CurrentPrice: 192; Increment: 7; Bidder: " + Main.SNIPER_ID + ";")
      translator.processMessage(UNUSED_CHAT, message)
      there was one(mockAuctionEventListener).auctionFailed
    }
  }


}
