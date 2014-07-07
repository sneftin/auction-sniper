package com.wix.slava.sniper

trait PortfolioListener {
  def sniperAdded(sniper: AuctionSniper)
}

class SniperPortfolio extends SniperCollector {

  var snipers = Set[AuctionSniper]()
  var listener: PortfolioListener = null

  override def addSniper(sniper: AuctionSniper) {
    snipers += sniper
    listener.sniperAdded(sniper)
  }

  def setPortfoliioListener(listener:PortfolioListener) {
    this.listener = listener
  }
}
