package com.wix.slava.sniper

import javax.swing.{JButton, JTextField}
import javax.swing.table.JTableHeader

import com.objogate.wl.swing.AWTEventQueueProber
import com.objogate.wl.swing.driver._
import com.objogate.wl.swing.gesture.GesturePerformer
import com.objogate.wl.swing.matcher.{JLabelTextMatcher, IterableComponentsMatcher}
import org.hamcrest.{Matchers, Matcher}
import org.hamcrest.Matchers.equalTo


object ApplicationRunner {
  //val SNIPER_XMPP_ID = "sniper"
  val SNIPER_XMPP_ID = "sniper@slavans-macbook-pro.local/Smack"
}

class ApplicationRunner {

  private val XMPP_HOSTNAME = "localhost"
  private val SNIPER_USER = "sniper"
  private val SNIPER_PASSWORD = "1234"

  var driver: AuctionSniperDriver = null

  def startBiddingIn(auctions:FakeAuctionServer*) { // TODO: to learn this syntax
    startSniper(auctions:_*)
    auctions.foreach(auction => {
      val itemId = auction.itemId
      driver.startBiddingFor(itemId)
      driver.showsSniperStatus(itemId, 0, 0, Main.STATUS_JOINING)
    })
  }

  private def startSniper(auctions:FakeAuctionServer*) {
    val t = new Thread("Test Application") {
      override def run {
        try {
          val mainApp = new Main
          mainApp.main(arguments(auctions:_*)) // TODO: to learn this syntax
        }
        catch {
          case e:Exception => e.printStackTrace()
        }
      }
    }
    t.setDaemon(true)
    t.start
    driver = new AuctionSniperDriver(1000)
    driver.hasTitle(MainWindow.MAIN_WINDOW_NAME)
    driver.hasColumnsTitles

    //auctions.foreach(fakeAuctionServer => driver.showsSniperStatus(fakeAuctionServer.itemId, 0, 0, Main.STATUS_JOINING))
  }

  private def arguments(auctions:FakeAuctionServer*) : Array[String] = {
    val args = new Array[String](3+auctions.length)
    args(0) = XMPP_HOSTNAME
    args(1) = SNIPER_USER
    args(2) = SNIPER_PASSWORD
    for (i <-0 to auctions.length-1)
      args(i+3) = auctions(i).itemId

    return args
  }

  def showsSniperHastLostAuction(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, Main.STATUS_LOST)
  }

  def hasShownSniperIsBidding(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, Main.STATUS_BIDDING)
  }

  def hasShownSniperIsWinning(auction: FakeAuctionServer, winningBid: Int) {
    driver.showsSniperStatus(auction.itemId, winningBid, winningBid, Main.STATUS_WINNING)
  }

  def showsSniperHastWonAuction(auction: FakeAuctionServer, lastPrice: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastPrice, Main.STATUS_WON)
  }

  def stop() {
    if (driver != null)
      driver.dispose()
  }
}

class AuctionSniperDriver (timeoutMillis : Int) extends JFrameDriver(new GesturePerformer(),
    JFrameDriver.topLevelFrame(ComponentDriver.named(MainWindow.MAIN_WINDOW_NAME),ComponentDriver.showingOnScreen()),
    new AWTEventQueueProber(timeoutMillis, 100)) {

  def startBiddingFor(itemId: String) = {
    itemIdField.replaceAllText(itemId)
    bidButton.click
  }

  private def itemIdField : JTextFieldDriver = {
    val newItemId = new JTextFieldDriver(this, classOf[JTextField], ComponentDriver.named(MainWindow.NEW_ITEM_ID_NAME))
    newItemId.focusWithMouse()
    newItemId
  }
  private def bidButton : JButtonDriver = {
    new JButtonDriver(this, classOf[JButton], ComponentDriver.named(MainWindow.JOIN_BUTTON_NAME))
  }

  def showsSniperStatus(itemId:String, lastPrice:Int, lastBid:Int, statusText : String) {

    //val labelDriver = new JLabelDriver(this, ComponentDriver.named(MainWindow.SNIPER_STATUS_NAME))
    //labelDriver.hasText(equalTo(statusText))

    val tableDriver = new JTableDriver(this)
    tableDriver.hasCell(JLabelTextMatcher.withLabelText(statusText))
    tableDriver.hasRow(IterableComponentsMatcher.matching(
      JLabelTextMatcher.withLabelText(itemId),
      JLabelTextMatcher.withLabelText(lastPrice.toString),
      JLabelTextMatcher.withLabelText(lastBid.toString),
      JLabelTextMatcher.withLabelText(statusText)))
    /*new JTableDriver(this).hasRow(IterableComponentsMatcher.matching(
      JLabelTextMatcher.withLabelText(statusText)
    ))*/
  }

  def hasColumnsTitles {
    val headers = new JTableHeaderDriver(this, classOf[JTableHeader])
    headers.hasHeaders(IterableComponentsMatcher.matching(
      JLabelTextMatcher.withLabelText("Item"),
      JLabelTextMatcher.withLabelText("Last Price"),
      JLabelTextMatcher.withLabelText("Last Bid"),
      JLabelTextMatcher.withLabelText("State")))
  }
}



