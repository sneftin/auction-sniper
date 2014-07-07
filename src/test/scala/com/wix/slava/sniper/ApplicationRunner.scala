package com.wix.slava.sniper

import java.io.File
import java.util.logging.LogManager
import javax.swing.{JButton, JTextField}
import javax.swing.table.JTableHeader

import com.objogate.wl.swing.AWTEventQueueProber
import com.objogate.wl.swing.driver._
import com.objogate.wl.swing.gesture.GesturePerformer
import com.objogate.wl.swing.matcher.{JLabelTextMatcher, IterableComponentsMatcher}
import com.wix.slava.sniper.ui.MainWindow
import org.hamcrest.{Matchers, Matcher}
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers.containsString

import scala.io.Source


object ApplicationRunner {
  //val SNIPER_XMPP_ID = "sniper"
  val SNIPER_XMPP_ID = "sniper@slavans-macbook-pro.local/Smack"
  val SNIPER_USER = "sniper"
  val SNIPER_PASSWORD = "1234"
}

class ApplicationRunner {


  val XMPP_HOSTNAME = "localhost"

  var driver: AuctionSniperDriver = null
  val logDriver = new AuctionLogDriver

  def startBiddingIn(auctions:FakeAuctionServer*) { // TODO: to learn this syntax
    startSniper(auctions:_*)
    auctions.foreach(auction => {
      val itemId = auction.itemId
      driver.startBiddingFor(itemId, Int.MaxValue)
      driver.showsSniperStatus(itemId, 0, 0, Main.STATUS_JOINING)
    })
  }

  def startBiddingWithStopPrice(auction: FakeAuctionServer, stopPrice:Int) {
    startSniper(auction)
    driver.startBiddingFor(auction.itemId, stopPrice)
    driver.showsSniperStatus(auction.itemId, 0, 0, Main.STATUS_JOINING)
  }

  private def startSniper(auctions:FakeAuctionServer*) {
    logDriver.clearLog
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
    args(1) = ApplicationRunner.SNIPER_USER
    args(2) = ApplicationRunner.SNIPER_PASSWORD
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

  def hasShownSniperIsLosing(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, Main.STATUS_LOOSING)
  }

  def showsSniperHasFailed(auction: FakeAuctionServer) {
    driver.showsSniperStatus(auction.itemId, 0, 0, Main.STATUS_FAILED)
  }

  def reportsInvalidMessage(server: FakeAuctionServer, s: String) {
    logDriver.hasEntry(containsString(s))
  }


  def stop() {
    if (driver != null)
      driver.dispose()
  }
}

class AuctionLogDriver {
  val logFile = new File(XMPPAuctionHouse.LOG_FILE_NAME)

  def hasEntry(matcher: Matcher[String]) = {
    val fileContent = Source.fromFile(logFile).getLines.reduceLeft(_+_)
    assertThat(fileContent, matcher)
  }

  def clearLog {
    logFile.delete
    LogManager.getLogManager.reset
  }
}

class AuctionSniperDriver (timeoutMillis : Int) extends JFrameDriver(new GesturePerformer(),
    JFrameDriver.topLevelFrame(ComponentDriver.named(MainWindow.MAIN_WINDOW_NAME),ComponentDriver.showingOnScreen()),
    new AWTEventQueueProber(timeoutMillis, 100)) {

  def startBiddingFor(itemId: String) = {
    itemIdField.replaceAllText(itemId)
    bidButton.click
  }

  def startBiddingFor(itemId: String, stopPrice: Int) = {
    itemIdField.replaceAllText(itemId)
    itemStopPriceField.replaceAllText(stopPrice.toString)
    bidButton.click
  }

  private def itemIdField : JTextFieldDriver = {
    val newItemId = new JTextFieldDriver(this, classOf[JTextField], ComponentDriver.named(MainWindow.NEW_ITEM_ID_NAME))
    newItemId.focusWithMouse()
    newItemId
  }

  private def itemStopPriceField : JTextFieldDriver = {
    val newItemId = new JTextFieldDriver(this, classOf[JTextField], ComponentDriver.named(MainWindow.NEW_ITEM_STOP_PRICE_NAME))
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



