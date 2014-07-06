package com.wix.slava.sniper

import javax.swing.SwingUtilities

class SwingThreadSniperListener(ui:SniperListener) extends SniperListener {

  override def sniperStateChanged(snapshot:SniperSnapshot) {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        ui.sniperStateChanged(snapshot)
      }
    })
  }
}
