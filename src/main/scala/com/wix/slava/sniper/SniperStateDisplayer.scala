package com.wix.slava.sniper

import javax.swing.SwingUtilities

/**
 * Created by Slava_Neftin on 7/1/14.
 */
class SniperStateDisplayer(ui:MainWindow) extends SniperListener {

  override def sniperLost { showStatus(Main.STATUS_LOST) }

  override def sniperBidding { showStatus(Main.STATUS_BIDDING) }

  override def sniperWinning {showStatus(Main.STATUS_WINNING) }

  override def sniperWon { showStatus(Main.STATUS_WON) }

  private def showStatus(status:String) {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        ui.showStatus(status)
      }
    })
  }
}
