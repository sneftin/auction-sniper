package com.wix.slava.sniper

//import org.specs2.mutable.Specification
//import org.specs2.specification.{Scope, After}

import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope


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

class AuctionSniperEndToEndTestTest extends Specification {

  trait AuctionAndApplication extends Scope with After {
    val application = new ApplicationRunner()
    val auction = new FakeAuctionServer("item-54321")

    def after = {
      auction.stop()
      application.stop()
    }
  }

  "Auction Sniper" should {
    sequential

    "join auction and loose it after auction closed" in new AuctionAndApplication {

      auction.startSellingItem
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)
      auction.announceClosed
      application.showsSniperHastLostAuction
    }


    "make higher bid and loose" in new AuctionAndApplication {

      auction.startSellingItem
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding
      auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID)

      auction.announceClosed
      application.showsSniperHastLostAuction
    }

    "wins auction by bidding higher" in new AuctionAndApplication {

      auction.startSellingItem
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding
      auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID)
      application.hasShownSniperIsWinning
      auction.announceClosed
      application.showsSniperHastWonAuction
    }

  }
}
