package com.wix.slava.sniper

import java.util.logging.{SimpleFormatter, FileHandler}
import java.util.logging.Logger

import com.wix.slava.sniper.xmpp.{LoggingXMPPFailureReporter, XMPPAuction, Auction}
import org.jivesoftware.smack.XMPPConnection

trait AuctionHouse {
  def auctionFor(item: Item) : Auction
}

class XMPPAuctionException(message: String, cause: Exception) extends Exception(message, cause)

class XMPPAuctionHouse(conn: XMPPConnection) extends AuctionHouse {

  val AUCTION_RESOURCE = "Auction"
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE
  val LOGGER_NAME = "auction-sniper"

  val failureReporter = new LoggingXMPPFailureReporter(makeLogger)
  def disconnect { conn.disconnect() }

  override def auctionFor(item: Item): Auction = new XMPPAuction(conn, item.id, failureReporter)

  private def makeLogger = {
    val logger = Logger.getLogger(LOGGER_NAME)
    logger.setUseParentHandlers(false)
    logger.addHandler(simpleFileHandler)
    logger
  }

  private def simpleFileHandler = {
    try {
      val fh = new FileHandler(XMPPAuctionHouse.LOG_FILE_NAME)
      fh.setFormatter(new SimpleFormatter)
      fh
    }
    catch {
      case e:Exception => throw new XMPPAuctionException("Could not create logger FileHandler " + XMPPAuctionHouse.LOG_FILE_NAME, e)
    }
  }
}

object XMPPAuctionHouse {

  val LOG_FILE_NAME = "auction-sniper.log"

  def connect(host: String, user: String, pass: String): XMPPAuctionHouse = {
    val conn = new XMPPConnection(host)
    conn.connect()
    conn.login(user, pass)
    new XMPPAuctionHouse(conn)
  }
}