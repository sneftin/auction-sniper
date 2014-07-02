package com.wix.slava.sniper

import java.awt.event.{WindowEvent, WindowAdapter}
import javax.swing.SwingUtilities

import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.{Chat, MessageListener, XMPPConnection}

object Main {
  val STATUS_JOINING: String = "Joining"
  val STATUS_LOST: String = "Lost"
  val STATUS_BIDDING = "Bidding"
  val STATUS_WINNING = "Winning"
  val STATUS_WON = "Won"

  val JOIN_COMMAND_FORMAT = "SQL Version: 1.1; Command: JOIN;"
  val BID_COMMAND_FORMAT = "SQL Version: 1.1; Command: BID; Price: %d;"

  val SNIPER_ID = "SlavaSniper"
}

class Main {

  val ARG_HOSTNAME = 0
  val ARG_USERNAME = 1
  val ARG_PASSWORD = 2
  val ARG_ITEM_ID = 3

  val AUCTION_RESOURCE = "Auction"
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE

  var ui: MainWindow = null
  var notToBeGCed: Chat = null

  def main(args: Array[String]) {
    val conn = connectTo(args(ARG_HOSTNAME), args(ARG_USERNAME), args(ARG_PASSWORD))
    startUserInterface(conn)
    joinAuction(conn,args(ARG_ITEM_ID))
  }



  private def joinAuction(conn:XMPPConnection, itemId:String) {

    val chat = conn.getChatManager.createChat(auctionId(itemId,conn), null)
    notToBeGCed = chat

    val auction = new XMPPAuction(chat)
    auction.join
    chat.addMessageListener( new AuctionMessageTranslator(conn.getUser,
      new AuctionSniper(auction, new SniperStateDisplayer(ui))))
   }


  private def connectTo(host:String, user:String, pass:String) : XMPPConnection = {
    val conn = new XMPPConnection(host)
    conn.connect()
    conn.login(user, pass)
    return conn
  }

  private def auctionId(itemId:String, conn:XMPPConnection): String = {
    AUCTION_ID_FORMAT.format(itemId, conn.getServiceName)
  }

  private def startUserInterface(conn:XMPPConnection) {
    SwingUtilities.invokeAndWait(new Runnable {
      override def run(): Unit = {
        ui = new MainWindow()
        disconnectWhenUICloses(conn)
      }
    })
  }
  private def disconnectWhenUICloses(conn:XMPPConnection) {
    ui.addWindowListener(new WindowAdapter {
      override def windowClosed(e:WindowEvent) = conn.disconnect()
    })
  }

}
