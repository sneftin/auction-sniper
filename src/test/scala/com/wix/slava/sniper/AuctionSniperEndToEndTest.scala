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
    val auction = new FakeAuctionServer("item54321")
    val auction2 = new FakeAuctionServer("item65432")

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
      application.showsSniperHastLostAuction(auction, 0,0)
    }


    "make higher bid and loose" in new AuctionAndApplication {

      auction.startSellingItem
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction, 1000, 1098)
      auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID)

      auction.announceClosed
      application.showsSniperHastLostAuction(auction,1000,1098)
    }

    "wins auction by bidding higher" in new AuctionAndApplication {

      auction.startSellingItem
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction,1000,1098)
      auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID)
      application.hasShownSniperIsWinning(auction, 1098)
      auction.announceClosed
      application.showsSniperHastWonAuction(auction,1098)

    }

    "bids for multiple items" in new AuctionAndApplication {
      auction.startSellingItem
      auction2.startSellingItem

      application.startBiddingIn(auction, auction2)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)
      auction2.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID)

      auction2.reportPrice(500, 21, "other bidder")
      auction2.hasReceivedBid(521, ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID)
      auction2.reportPrice(521, 22, ApplicationRunner.SNIPER_XMPP_ID)

      application.hasShownSniperIsWinning(auction,1098)
      application.hasShownSniperIsWinning(auction2,521)

      auction.announceClosed
      auction2.announceClosed

      application.showsSniperHastWonAuction(auction,1098)
      application.showsSniperHastWonAuction(auction2,521)
    }

  }
}
