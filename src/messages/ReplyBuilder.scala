package messages

import messages._
import parsers.{CommandParser, Params}
import collection.mutable.ArrayBuffer
import targets.User

//TODO: I dislike this class. The method with communicating to clients needs to be overhauled
object ReplyBuilder {
    def create(dstNick: String, replyMessage: ReplyMessage) {

    }
}
class ReplyBuilder(user: User) {
    val _replies = new ArrayBuffer[Message]
    private val dstNick = user.record.nick

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

    def append(msg: Message):ReplyBuilder = {
        append(msg)
    }

    def get:String = {
        val sb = new StringBuilder

        _replies.foreach ((replyMessage) => {
            if (replyMessage.command != Replies.RPL_NONE.getCode) {
                sb.append(replyMessage.toString)
                sb.append("\n")
            }
        })

        sb.toString()
    }
}

