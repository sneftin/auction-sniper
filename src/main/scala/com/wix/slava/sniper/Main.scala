package com.wix.slava.sniper

import java.awt.event.{WindowEvent, WindowAdapter}
import javax.swing.SwingUtilities

import com.wix.slava.sniper.ui.{SwingThreadSniperListener, SnipersTableModel, UserRequestListener, MainWindow}
import com.wix.slava.sniper.xmpp.Auction

import scala.collection.mutable

object Main {
  val STATUS_JOINING: String = "Joining"
  val STATUS_LOST: String = "Lost"
  val STATUS_BIDDING = "Bidding"
  val STATUS_WINNING = "Winning"
  val STATUS_WON = "Won"
  val STATUS_LOOSING = "Losing"
  val STATUS_FAILED = "Failed"

  val JOIN_COMMAND_FORMAT = "SQL Version: 1.1; Command: JOIN;"
  val BID_COMMAND_FORMAT = "SQL Version: 1.1; Command: BID; Price: %d;"

  val SNIPER_ID = "SlavaSniper"
}

class Main {

  val ARG_HOSTNAME = 0
  val ARG_USERNAME = 1
  val ARG_PASSWORD = 2
  val ARG_ITEM_ID = 3


  var ui: MainWindow = null
  var auctionHouse: XMPPAuctionHouse = null
  val portfolio = new SniperPortfolio

  def main(args: Array[String]) {
    auctionHouse = XMPPAuctionHouse.connect(args(ARG_HOSTNAME), args(ARG_USERNAME), args(ARG_PASSWORD))
    startUserInterface
    disconnectWhenUICloses(auctionHouse)
    addUserRequestListenerFor(auctionHouse)
  }


  private def addUserRequestListenerFor(auctionHouse:AuctionHouse) {
    ui.addUserRequestListener(new SniperLauncher(auctionHouse, portfolio))
  }


  private def startUserInterface {
    SwingUtilities.invokeAndWait(new Runnable {
      override def run(): Unit = {
        ui = new MainWindow(portfolio)
      }
    })
  }
  private def disconnectWhenUICloses (auctionHouse:XMPPAuctionHouse) {
    ui.addWindowListener(new WindowAdapter {
      override def windowClosed(e:WindowEvent) = auctionHouse.disconnect
    })
  }

}
