package com.wix.slava.sniper.integration

import com.objogate.wl.swing.probe.ValueMatcherProbe
import com.wix.slava.sniper.{Item, SniperPortfolio, AuctionSniperDriver}
import com.wix.slava.sniper.ui.{MainWindow, UserRequestListener}
import org.hamcrest.Matchers
import org.specs2.mutable.Specification
import org.specs2.specification.Scope


class MainWindowTest extends Specification {

  trait Context extends Scope {
    val mainWindow = new MainWindow(new SniperPortfolio)
    val driver = new AuctionSniperDriver(100)
 }

  "MainWindow" should {
    "make user request when join button clicked" in new Context {

      val itemProbe = new ValueMatcherProbe[Item](Matchers.equalTo(new Item("12345", 789)), "join request")

      mainWindow.addUserRequestListener(new UserRequestListener {
        def joinAuction(item: Item) = {
          itemProbe.setReceivedValue(item)
        }
      })

      driver.startBiddingFor("12345", 789)
      driver.check(itemProbe)
      driver.dispose()
    }
  }
}
