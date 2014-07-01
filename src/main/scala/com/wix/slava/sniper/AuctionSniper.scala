package com.wix.slava.sniper

trait SniperListener {
  def sniperLost

  def sniperBidding
}

class AuctionSniper(auction:Auction, sniperListener:SniperListener) extends AuctionEventListener {

  override def auctionClosed {
    sniperListener.sniperLost
  }

  override def currentPrice(price:Int, increment:Int) {
    println("AuctionSniper::currentPrice called")
    auction.bid(price+increment)
    sniperListener.sniperBidding
  }
}
