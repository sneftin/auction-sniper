package com.wix.slava.sniper

import java.awt.Color
import javax.swing.border.LineBorder
import javax.swing.{JLabel, JFrame}

import org.jivesoftware.smack.Chat


class MainWindow extends JFrame("Auction Sniper") {

  val sniperStatus = createLabel(Main.STATUS_JOINING)
  
  setName(MainWindow.MAIN_WINDOW_NAME)
  add(sniperStatus)
  pack()
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  setVisible(true)

  def createLabel(initialText: String): JLabel = {
    val label = new JLabel(initialText)
    label.setName(MainWindow.SNIPER_STATUS_NAME)
    label.setBorder(new LineBorder(Color.BLACK))
    return label
  }
  def showStatus(status: String) {
    sniperStatus.setText(status)
  }

}

object MainWindow {
  val MAIN_WINDOW_NAME = "Auction Sniper Main"
  val SNIPER_STATUS_NAME = "Status"
}
