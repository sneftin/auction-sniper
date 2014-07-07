package com.wix.slava.sniper

import com.wix.slava.sniper.ui.{SwingThreadSniperListener, SnipersTableModel, UserRequestListener}

class SniperLauncher (auctionHouse: AuctionHouse, sniperCollector: SniperCollector) extends UserRequestListener {


  override def joinAuction(item: Item) {

      val auction = auctionHouse.auctionFor(item)
      val sniper = new AuctionSniper(item, auction)
      auction.addAuctionEventListener(sniper)
      sniperCollector.addSniper(sniper)
      auction.join
  }

}
