package com.wix.slava.sniper


import org.specs2.matcher.{BeTrueMatcher, Matcher}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.{Then, Scope}

import com.wix.slava.sniper.PriceSourceEnum._


class AuctionSniperTest extends Specification with Mockito {

  val ITEM_ID = "12345"

  trait Context extends Scope {
    val sniperListener = mock[SniperListener]
    val auction = mock[Auction]
    val sniper = new AuctionSniper(ITEM_ID, auction, sniperListener)
  }

  def beInState(state: String): Matcher[String] =
    ((_: String) == state, s"isn't in state $state")

  def aSniperThatIs(state: SniperState.Value): Matcher[SniperSnapshot] =
    ((_: SniperSnapshot).state == state, s"isn't in state $state")

  "Auction Sniper" should {
    "report lost when auction closes immediately" in new Context {
      sniper.auctionClosed
      there was one(sniperListener).sniperStateChanged(aSniperThatIs(SniperState.Lost))
    }
  }

  "Auction Sniper" should {
    "report lost when auction closes when bidding" in new Context {
      sniper.currentPrice(123, 45, FromOtherBidder)
      sniper.auctionClosed

      /*there was
        atLeastOne(sniperListener).sniperStateChanged(any[SniperSnapshot]) andThen
        one(sniperListener).sniperLost
      "win" must beInState("win")
      */

      there was
        atLeastOne(sniperListener).sniperStateChanged(aSniperThatIs(SniperState.Bidding)) andThen
        one(sniperListener).sniperStateChanged(aSniperThatIs(SniperState.Lost))
    }
  }


  "Auction Sniper" should {
    "big higher and responds bidding when new price arrives" in new Context {
      val price = 1001
      val increment = 25
      val bid = price + increment
      sniper.currentPrice(price, increment, FromOtherBidder)
      there was one(auction).bid(price + increment)
      there was atLeastOne(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, SniperState.Bidding))
    }
  }

  "Auction Sniper" should {
    "report winning when current price coming from price" in new Context {
      sniper.currentPrice(123, 12, FromOtherBidder)
      there was atLeastOne(sniperListener).sniperStateChanged(aSniperThatIs(SniperState.Bidding))
      sniper.currentPrice(135, 45, FromSniper)
      there was atLeastOne(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID,135,135,SniperState.Winning))
    }
  }

  "Auction Sniper" should {
    "report won when auction close when winning" in new Context {
      sniper.currentPrice(123, 45, FromSniper)
      sniper.auctionClosed
      there was one(sniperListener).sniperStateChanged(aSniperThatIs(SniperState.Winning))
    }
  }

}