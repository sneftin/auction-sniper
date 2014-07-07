package com.wix.slava.sniper.integration

import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.wix.slava.sniper.PriceSourceEnum.PriceSource
import com.wix.slava.sniper.{XMPPAuctionHouse, ApplicationRunner, AuctionEventListener, FakeAuctionServer}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import com.wix.slava.sniper.Item


class XMPPAuctionHouseTest extends Specification {

  trait Context extends Scope {
    val auctionServer = new FakeAuctionServer("item54321")
  }

  "XMPPAuction" should {
    "receive events from auction server after joining" in new Context {

      auctionServer.startSellingItem

      val auctionHouse = XMPPAuctionHouse.connect(FakeAuctionServer.XMPP_HOSTNAME, ApplicationRunner.SNIPER_USER, ApplicationRunner.SNIPER_PASSWORD);
      val auctionWasClosed = new CountDownLatch(1)
      val auction = auctionHouse.auctionFor(new Item(auctionServer.itemId, 567))

      auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed))
      auction.join
      auctionServer.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)
      auctionServer.announceClosed

      auctionWasClosed.await(2, TimeUnit.SECONDS) must beTrue
    }
  }

  def auctionClosedListener(auctionWasClosed:CountDownLatch) : AuctionEventListener = {
    new AuctionEventListener {
      override def currentPrice(price: Int, increment: Int, priceSource: PriceSource): Unit = ???
      override def auctionClosed { auctionWasClosed.countDown() }
      override def auctionFailed {}
    }
  }
}
