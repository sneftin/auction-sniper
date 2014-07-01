package com.wix.slava.sniper

import java.util.EventListener

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Message
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

/**
 * Created by Slava_Neftin on 7/1/14.
 */



class AuctionSniperTest extends Specification with Mockito {

  trait Context extends Scope {
    val sniperListener = mock[SniperListener]
    val auction = mock[Auction]
    val sniper = new AuctionSniper(auction, sniperListener)
  }

  "Auction Sniper" should {
    "report lost when auction closes" in new Context {
      sniper.auctionClosed
      there was one(sniperListener).sniperLost
    }
  }

  "Auction Sniper" should {
    "big higher and responds bidding when new price arrives" in new Context {
      val price = 1001
      val increment = 25
      sniper.currentPrice(price, increment)
      there was one(auction).bid(price+increment)
      there was atLeastOne(sniperListener).sniperBidding
    }
  }
}