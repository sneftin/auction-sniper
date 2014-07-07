package com.wix.slava.sniper.xmpp

import com.wix.slava.sniper.PriceSourceEnum.PriceSource
import com.wix.slava.sniper.{Item, Announcer, Main, AuctionEventListener}
import org.jivesoftware.smack.XMPPConnection

trait Auction {
  def bid(price:Int)
  def addAuctionEventListener(auctionEventListener:AuctionEventListener)
  def join
}

class XMPPAuction(conn:XMPPConnection, itemId: String, failureReporter: XMPPFailureReporter) extends Auction {

  val AUCTION_RESOURCE = "Auction"
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE

  val auctionEventListeners: Announcer[AuctionEventListener] = Announcer.to[AuctionEventListener](classOf[AuctionEventListener])

  val translator = translatorFor((conn))
  val chat = conn.getChatManager.createChat(auctionId(itemId, conn), translator)
  addAuctionEventListener(chatDisconnectorFor(translator))

  private def translatorFor(conn: XMPPConnection) = {
    new AuctionMessageTranslator(conn.getUser, auctionEventListeners.announce(), failureReporter)
  }

  private def chatDisconnectorFor(translator: AuctionMessageTranslator) = {
    new AuctionEventListener {
      override def auctionFailed { chat.removeMessageListener(translator) }
      override def currentPrice(price: Int, increment: Int, priceSource: PriceSource) {}
      override def auctionClosed {}
    }
  }

  def addAuctionEventListener(auctionEventListener:AuctionEventListener) {
    auctionEventListeners.addListener(auctionEventListener)
  }

  private def auctionId(itemId:String, conn:XMPPConnection): String = {
    AUCTION_ID_FORMAT.format(itemId, conn.getServiceName)
  }

  override def bid(price: Int) {
    sendMessage(Main.BID_COMMAND_FORMAT.format(price))
  }

  def join {
    sendMessage(Main.JOIN_COMMAND_FORMAT)
  }

  private def sendMessage(message:String) {
    try {
      chat.sendMessage(message)
    }
    catch {
      case e:Exception => e.printStackTrace()
    }
  }
}
