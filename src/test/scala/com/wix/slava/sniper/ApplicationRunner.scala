package com.wix.slava.sniper

import com.objogate.wl.swing.AWTEventQueueProber
import com.objogate.wl.swing.driver.{JTableDriver, JLabelDriver, ComponentDriver, JFrameDriver}
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

  def startBiddingIn(auction:FakeAuctionServer) {
    val t = new Thread("Test Application") {
      override def run {
        try {
          val mainApp = new Main
          mainApp.main(Array(XMPP_HOSTNAME, SNIPER_USER, SNIPER_PASSWORD, auction.itemId))
        }
        catch {
          case e:Exception => e.printStackTrace()
        }
      }
    }
    t.setDaemon(true)
    t.start
    driver = new AuctionSniperDriver(1000)
    driver.showsSniperStatus(Main.STATUS_JOINING)
  }

  def showsSniperHastLostAuction() {
    driver.showsSniperStatus(Main.STATUS_LOST)
  }

  def hasShownSniperIsBidding() {
    driver.showsSniperStatus(Main.STATUS_BIDDING)
  }

  def hasShownSniperIsWinning {
    driver.showsSniperStatus(Main.STATUS_WINNING)
  }

  def showsSniperHastWonAuction() {
    driver.showsSniperStatus(Main.STATUS_WON)
  }

  def stop() {
    if (driver != null)
      driver.dispose()
  }
}

class AuctionSniperDriver (timeoutMillis : Int) extends JFrameDriver(new GesturePerformer(),
    JFrameDriver.topLevelFrame(ComponentDriver.named(MainWindow.MAIN_WINDOW_NAME),ComponentDriver.showingOnScreen()),
    new AWTEventQueueProber(timeoutMillis, 100)) {

  def showsSniperStatus(statusText : String) {
    // TODO: why this not compilable
    val labelDriver = new JLabelDriver(this, ComponentDriver.named(MainWindow.SNIPER_STATUS_NAME))
    labelDriver.hasText(equalTo(statusText))

    /*new JTableDriver(this).hasRow(IterableComponentsMatcher.matching(
      JLabelTextMatcher.withLabelText(statusText)
    ))*/
  }
}



