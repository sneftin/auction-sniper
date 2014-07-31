package com.wix.slava.sniper

import java.util.EventListener

import com.wix.slava.sniper.PriceSourceEnum._
import com.wix.slava.sniper.xmpp.Auction

trait SniperListener extends EventListener {
  def sniperStateChanged(state:SniperSnapshot)
}

//TODO: Not to use Enumeration but sealed abstract case classes
object SniperState extends Enumeration {
  val Joining = Value
  val Bidding = Value
  val Winning = Value
  val Lost = Value
  val Won = Value
  val Losing = Value
  val Failed = Value
}

case class SniperSnapshot(itemId:String, lastPrice:Int, lastBid:Int, state:SniperState.Value) {

  def bidding(newLastPrice:Int, newLastBid:Int) = SniperSnapshot(itemId, newLastPrice, newLastBid, SniperState.Bidding)
  def winning(newLastPrice:Int) = SniperSnapshot(itemId, newLastPrice, lastBid, SniperState.Winning)
  def losing(newLastPrice:Int) = SniperSnapshot(itemId, newLastPrice, lastBid, SniperState.Losing)
  def failed  = SniperSnapshot(itemId, 0, 0, SniperState.Failed)
  def closed = SniperSnapshot(itemId, lastPrice, lastBid,
    state match {
      case SniperState.Winning  => SniperState.Won
      case SniperState.Joining | SniperState.Bidding | SniperState.Losing => SniperState.Lost
      case _ => throw new Exception("Auction is already closed")
    })

}

object SniperSnapshot {
  def joining(itemId: String) = SniperSnapshot(itemId, 0, 0, SniperState.Joining)
}

class AuctionSniper(item: Item, auction:Auction) extends AuctionEventListener {

  val listeners:Announcer[SniperListener] = Announcer.to[SniperListener](classOf[SniperListener])

  var snapshot = SniperSnapshot.joining(item.id)
  //notifyChange

  def addSniperListener(listener: SniperListener) {
    listeners.addListener(listener)
  }

  override def auctionClosed {
    snapshot = snapshot.closed
    notifyChange
  }

  override def auctionFailed {
    snapshot= snapshot.failed
    listeners.announce().sniperStateChanged(snapshot)
  }

  override def currentPrice(price:Int, increment:Int, priceSource:PriceSource) {
    priceSource match {
      case FromSniper => snapshot = snapshot.winning(price)
      case FromOtherBidder => {
        val bid = price + increment
        if (item.allowsBid(bid)) {
          auction.bid(bid)
          snapshot = snapshot.bidding(price, bid)
        }
        else {
          snapshot = snapshot.losing(price)
        }
      }
    }
    notifyChange
  }

  private def notifyChange {
    listeners.announce().sniperStateChanged(snapshot)
  }
}
