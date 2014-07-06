package com.wix.slava.sniper

import java.awt.event.{WindowEvent, WindowAdapter}
import javax.swing.SwingUtilities

import com.wix.slava.sniper.ui.{SwingThreadSniperListener, SnipersTableModel, UserRequestListener, MainWindow}
import org.jivesoftware.smack.{Chat, MessageListener, XMPPConnection}

import scala.collection.mutable

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

  val snipers = new SnipersTableModel
  var ui: MainWindow = null
  var notToBeGCed = mutable.MutableList[Chat]()

  def main(args: Array[String]) {
    val connection = connectTo(args(ARG_HOSTNAME), args(ARG_USERNAME), args(ARG_PASSWORD))
    startUserInterface
    disconnectWhenUICloses(connection)
    addUserRequestListenerFor(connection)
    //for (i <- 3 to args.length-1)
    //  joinAuction(connection, args(i))
  }

  private def addUserRequestListenerFor(conn:XMPPConnection) {
    ui.addUserRequestListener(new UserRequestListener {
      override def joinAuction(itemId: String) {
        snipers.addSniper(SniperSnapshot.joining(itemId))
        val chat = conn.getChatManager.createChat(auctionId(itemId,conn), null)
        notToBeGCed += chat
        val auction = new XMPPAuction(chat)
        chat.addMessageListener( new AuctionMessageTranslator(conn.getUser,
          new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers))))
        auction.join
      }
    })
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

  private def startUserInterface {
    SwingUtilities.invokeAndWait(new Runnable {
      override def run(): Unit = {
        ui = new MainWindow(snipers)
      }
    })
  }
  private def disconnectWhenUICloses(conn:XMPPConnection) {
    ui.addWindowListener(new WindowAdapter {
      override def windowClosed(e:WindowEvent) = conn.disconnect()
    })
  }

}
