package com.wix.slava.sniper

import com.wix.slava.sniper.PriceSourceEnum._

trait SniperListener {
  def sniperStateChanged(state:SniperSnapshot)
}

object SniperState extends Enumeration {
  val Joining = Value
  val Bidding = Value
  val Winning = Value
  val Lost = Value
  val Won = Value
}

case class SniperSnapshot(itemId:String, lastPrice:Int, lastBid:Int, state:SniperState.Value) {
  def bidding(newLastPrice:Int, newLastBid:Int) = SniperSnapshot(itemId, newLastPrice, newLastBid, SniperState.Bidding)
  def winning(newLastPrice:Int) = SniperSnapshot(itemId, newLastPrice, lastBid, SniperState.Winning)
  def closed = SniperSnapshot(itemId, lastPrice, lastBid,
    state match {
      case SniperState.Winning  => SniperState.Won
      case SniperState.Joining | SniperState.Bidding => SniperState.Lost
      case _ => throw new Exception("Auction is already closed")
    })

}

object SniperSnapshot {
  def joining(itemId: String) = SniperSnapshot(itemId, 0, 0, SniperState.Joining)
}

class AuctionSniper(itemId:String, auction:Auction, sniperListener:SniperListener) extends AuctionEventListener {

  var snapshot = SniperSnapshot.joining(itemId)
  notifyChange

  override def auctionClosed {
    snapshot = snapshot.closed
    notifyChange
  }

  override def currentPrice(price:Int, increment:Int, priceSource:PriceSource) {
    priceSource match {
      case FromSniper => snapshot = snapshot.winning(price)
      case FromOtherBidder => {
        val bid = price + increment
        auction.bid(bid)
        snapshot = snapshot.bidding(price, bid)
      }
    }
    notifyChange
  }

  private def notifyChange {
    sniperListener.sniperStateChanged(snapshot)
  }
}
