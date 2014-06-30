package com.wix.slava.sniper

import java.util.concurrent.{TimeUnit, ArrayBlockingQueue}

import org.jivesoftware.smack._
import org.jivesoftware.smack.packet.Message
import org.hamcrest.MatcherAssert._
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.is

case class FakeAuctionServer(itemId : String) {

  val AUCTION_USER_AS_ITEM_ID = "auction-%s"
  val AUCTION_RESOURCE = "Auction"
  val XMPP_HOSTNAME = "localhost"
  val AUCTION_PASSWORD = "1234"
  
  val conn = new XMPPConnection(XMPP_HOSTNAME)
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
  def hasReceivedJoinRequestFromSniper {
    msgListener.receivesAMessage
  }

  def announceClosed {
    currentChat.sendMessage(new Message())
  }

  def stop() {
    conn.disconnect()
  }
}

class SingleMessageListener extends MessageListener {

  val messages = new ArrayBlockingQueue[Message](1)

  override def processMessage(chat: Chat, message: Message): Unit = {
    messages.add(message)
  }

  def receivesAMessage {
    assertThat("Expected message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue()))
  }
}
