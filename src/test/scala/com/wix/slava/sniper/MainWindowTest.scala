package com.wix.slava.sniper

import com.objogate.wl.swing.probe.ValueMatcherProbe
import org.hamcrest.Matchers
import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope


class MainWindowTest extends Specification {

  trait Context extends Scope {
    val snipersTableModel = new SnipersTableModel()
    val mainWindow = new MainWindow(snipersTableModel)
    val driver = new AuctionSniperDriver(100)
 }

  "MainWindow" should {
    "make user request when join button clicked" in new Context {

      val buttonProbe = new ValueMatcherProbe[String](Matchers.equalTo("12345"), "join request")

      mainWindow.addUserRequestListener(new UserRequestListener {
        def joinAuction(itemId: String) = {
          buttonProbe.setReceivedValue(itemId)
        }
      })

      driver.startBiddingFor("12345")
      driver.check(buttonProbe)
      driver.dispose()
    }
  }
}
