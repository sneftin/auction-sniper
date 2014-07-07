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
      auction2.stop() // call stop if when not connected (there is a chaeck inside)
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

    "looses when price is too high" in new AuctionAndApplication {

      auction.startSellingItem
      application.startBiddingWithStopPrice(auction, 1100)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction,1000,1098)
      auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(1197, 10, "third party")
      application.hasShownSniperIsLosing(auction, 1197, 1098)
      auction.reportPrice(1207, 10, "fourth party")
      application.hasShownSniperIsLosing(auction, 1207, 1098)

      auction.announceClosed
      application.showsSniperHastLostAuction(auction, 1207, 1098)
    }


    "bids for multiple items" in new AuctionAndApplication {
      val brokenMessage = "a broken message"
      auction.startSellingItem
      auction2.startSellingItem

      application.startBiddingIn(auction, auction2)
      auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)

      auction.reportPrice(500, 20, "other bidder")
      auction.hasReceivedBid(520, ApplicationRunner.SNIPER_XMPP_ID)

      auction.sendInvalidMessageContaining(brokenMessage)
      application.showsSniperHasFailed(auction)

      auction.reportPrice(521, 21, "other bidder")
      waitForAnotherAuctionEvent

      application.reportsInvalidMessage(auction, brokenMessage)
      application.showsSniperHasFailed(auction)

      def waitForAnotherAuctionEvent {
        auction2.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID)
        auction2.reportPrice(600, 6, "other bidder")
        application.hasShownSniperIsBidding(auction2, 600, 606)
      }
    }

  }
}
