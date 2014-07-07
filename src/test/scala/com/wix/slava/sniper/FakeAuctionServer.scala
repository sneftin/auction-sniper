package com.wix.slava.sniper

import java.util.concurrent.{TimeUnit, ArrayBlockingQueue}

import org.hamcrest.{Matcher, Matchers}
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet.Message
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.is
import org.hamcrest.Matchers.equalTo

object FakeAuctionServer {
  val XMPP_HOSTNAME = "localhost"
}

case class FakeAuctionServer(itemId : String) {


  val AUCTION_USER_AS_ITEM_ID = "auction-%s"
  val AUCTION_RESOURCE = "Auction"
  val AUCTION_PASSWORD = "1234"
  
  val conn = new XMPPConnection(FakeAuctionServer.XMPP_HOSTNAME)
  var currentChat:Chat = null

  val msgListener = new SingleMessageListener()

  def startSellingItem {
    conn.connect()
    conn.login(AUCTION_USER_AS_ITEM_ID.format(itemId), AUCTION_PASSWORD, AUCTION_RESOURCE)
    conn.getChatManager.addChatListener(new ChatManagerListener {
      override def chatCreated(chat: Chat, createdLocally: Boolean): Unit = {
        currentChat = chat
        currentChat.addMessageListener(msgListener)
      }
    })
  }

  def reportPrice(price: Int, increment: Int, bidder: String) {
    currentChat.sendMessage("SQLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;".format(price,
    increment, bidder))
  }

  def sendInvalidMessageContaining(message: String) {
    currentChat.sendMessage(message)
  }


  def hasReceivedJoinRequestFromSniper(sniperId:String) {
    receivedAMessageMatching(sniperId, equalTo(Main.JOIN_COMMAND_FORMAT))
  }

  def hasReceivedBid(bid: Int, sniperId:String) {
    assertThat(currentChat.getParticipant, equalTo(sniperId))
    receivedAMessageMatching(sniperId, equalTo(Main.BID_COMMAND_FORMAT.format(bid)))
  }

  private def receivedAMessageMatching(sniperId: String, msgMatcher: Matcher[String]) {
    msgListener.receivesAMessage(msgMatcher)
    assertThat(currentChat.getParticipant, equalTo(sniperId))
  }

  def announceClosed {
    currentChat.sendMessage("SQLVersion: 1.1; Event: CLOSE;")
  }

  def stop() {
    if (conn.isConnected)
      conn.disconnect()
  }
}

class SingleMessageListener extends MessageListener {

  val messages = new ArrayBlockingQueue[Message](1)

  override def processMessage(chat: Chat, message: Message): Unit = {
    messages.add(message)
  }

  def receivesAMessage(msgMatcher: Matcher[String]) {
    val message = messages.poll(5, TimeUnit.SECONDS)
    assertThat("Expected message", message, is(notNullValue()))
    assertThat("Message", message.getBody(), msgMatcher)
  }
}
