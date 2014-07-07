package com.wix.slava.sniper.unit

import com.wix.slava.sniper.xmpp.Auction
import com.wix.slava.sniper._
import org.specs2.matcher.Matcher
import org.specs2.mock.Mockito
import org.specs2.mutable.{Before, Specification}
import org.specs2.specification.Scope

class SniperLauncherTest extends Specification with Mockito {

  trait Context extends Scope {

    val auctionHouse = mock[AuctionHouse]
    val sniperCollector = mock[SniperCollector]
    val auction = mock[Auction]
    val launcher = new SniperLauncher(auctionHouse, sniperCollector)
  }

  def withSniperForItem(itemId:String) : Matcher[AuctionSniper] = {
    ((_:AuctionSniper).snapshot.itemId == itemId, s"hsn;t correct item id $itemId")
  }

  "SniperLauncer" should {
    "add sniper to collection and then join auction" in new Context {
      val item =  new Item("item 123",1000)
      launcher.joinAuction(item)
      //TODO: why this test is failing
      there was one(sniperCollector).addSniper(withSniperForItem(item.id))
      there was one(auction).addAuctionEventListener(withSniperForItem(item.id))
      there was one(auction).join
    }
  }
}
