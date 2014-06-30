package com.wix.slava.sniper

import javax.swing.SwingUtilities

import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.{Chat, MessageListener, XMPPConnection}

object Main {
  val STATUS_JOINING: String = "Joining"
  val STATUS_LOST: String = "Lost"

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
    startUserInterface
    val conn = connectTo(args(ARG_HOSTNAME), args(ARG_USERNAME), args(ARG_PASSWORD))
    val chat = conn.getChatManager.createChat(auctionId(args(ARG_ITEM_ID),conn), new MessageListener {
      override def processMessage(chat: Chat, message: Message): Unit = {
        SwingUtilities.invokeLater(new Runnable {
          override def run(): Unit = {
            ui.showStatus(STATUS_LOST)
          }
        })
      }
    })
    notToBeGCed = chat
    chat.sendMessage(new Message())
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
        ui = new MainWindow()
      }
    })
  }
}
