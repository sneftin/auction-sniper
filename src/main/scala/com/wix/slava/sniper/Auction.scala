package com.wix.slava.sniper

import org.jivesoftware.smack.Chat

trait Auction {
  def bid(price:Int)
}

class XMPPAuction(chat:Chat) extends Auction {
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
