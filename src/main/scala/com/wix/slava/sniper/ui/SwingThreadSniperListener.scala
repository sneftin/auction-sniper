package com.wix.slava.sniper.ui

import javax.swing.SwingUtilities

import com.wix.slava.sniper.{SniperListener, SniperSnapshot}

class SwingThreadSniperListener(ui:SniperListener) extends SniperListener {

  override def sniperStateChanged(snapshot:SniperSnapshot) {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        ui.sniperStateChanged(snapshot)
      }
    })
  }
}
