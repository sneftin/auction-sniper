package com.wix.slava.sniper

import com.wix.slava.sniper.PriceSourceEnum._

trait SniperListener {
  def sniperLost
  def sniperBidding
  def sniperWinning
  def sniperWon
}

class AuctionSniper(auction:Auction, sniperListener:SniperListener) extends AuctionEventListener {

  var isWining = false

  override def auctionClosed {
    if (isWining)
      sniperListener.sniperWon
    else
      sniperListener.sniperLost
  }

  override def currentPrice(price:Int, increment:Int, priceSource:PriceSource) {
    isWining = (priceSource == FromSniper)
    if (isWining)
      sniperListener.sniperWinning
    else {
      auction.bid(price+increment)
      sniperListener.sniperBidding
    }
    /*priceSource match {
      case FromSniper => { sniperListener.sniperWinning }
      case FromOtherBidder => {
        auction.bid(price+increment)
        sniperListener.sniperBidding
      }
    }*/
  }
}
