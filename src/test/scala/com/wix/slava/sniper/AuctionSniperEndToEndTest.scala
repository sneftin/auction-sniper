package com.wix.slava.sniper

import org.specs2.mutable.Specification
import org.specs2.specification.{Scope, After}

/*class AuctionSniperEndToEndTest {
  val auction = new FakeAuctionServer("item-54321")
  val application = new ApplicationRunner()

  @Test
  def sniperJoinsAuctionUntilAuctionCloses {
    auction.startSellingItem
    application.startBiddingIn(auction)
    auction.hasReceivedJoinRequestFromSniper
    auction.announceClosed
    application.showsSniperHastLostAuction
  }
}
*/

class Spec2AuctionSniperEndToEndTest extends Specification {

  trait AuctionAndApplication extends After {
    val auction = new FakeAuctionServer("item-54321")
    val application = new ApplicationRunner()

    override def after: Any = {
      auction.stop()
      application.stop()
    }
  }

  "Auction Sniper" should {
    sequential

    "join auction and loose it after auction closed" in new AuctionAndApplication {
      auction.startSellingItem
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper
      auction.announceClosed
      application.showsSniperHastLostAuction
    }

    /*
    "join again auction and lost it after auction closed" in new AuctionAndApplication {

      auction.startSellingItem
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper
      auction.announceClosed
      application.showsSniperHastLostAuction
    }
    */
  }

}
