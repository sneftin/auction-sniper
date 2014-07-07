package com.wix.slava.sniper.xmpp

import java.util.logging.Logger

/**
 * Created by Slava_Neftin on 7/7/14.
 */
class LoggingXMPPFailureReporter(logger: Logger) extends XMPPFailureReporter {

  val MESSAGE_FORMAT = "<%s> Could not translate message \"%s\" because \"%s\""
  override def cannotTranslateMessage(auctionId: String, failedMessage: String, exception: Exception) {
    logger.severe(MESSAGE_FORMAT.format(auctionId, failedMessage, exception.toString))
  }
}
