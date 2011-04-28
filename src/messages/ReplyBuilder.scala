package messages

import messages._
import parsers.{CommandParser, Params}
import db.User
import collection.mutable.ArrayBuffer

//TODO: I dislike this class. The method with communicating to clients needs to be overhauled
object ReplyBuilder {
    def create(dstNick: String, replyMessage: ReplyMessage) {

    }
}
class ReplyBuilder(dstNick: String) {
    val _replies = new ArrayBuffer[ReplyMessage]

    def append(replyMsg: ReplyMessage):ReplyBuilder = {
        _replies.append(replyMsg)
        this
    }

    def append(replyBuilder: ReplyBuilder):ReplyBuilder = {
        _replies ++= replyBuilder._replies
        this
    }

    def append(reply: Reply):ReplyBuilder = {
        append(new ReplyMessage(dstNick, reply))
    }

    def get:String = {
        val sb = new StringBuilder

        _replies.foreach ((replyMessage) => {
            if (replyMessage.reply != Replies.RPL_NONE) {
                sb.append(replyMessage.toString)
                sb.append("\n")
            }
        })

        sb.toString()
    }
}

