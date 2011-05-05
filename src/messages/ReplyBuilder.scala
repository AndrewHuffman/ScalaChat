package messages

import messages._
import parsers.{ParameterParser, Params}
import collection.mutable.ArrayBuffer
import targets.User

/**
 * ReplyBuilder
 *
 * Allows Message's, Reply's, and other ReplyBuilders to be appended to
 * the reply that will be sent to the user once command execution is complete.
 */
class ReplyBuilder(user: User) {
    val _replies = new ArrayBuffer[Message]
    private val dstNick = user.record.nick

    /**
     * Appends the given and returns this
     */
    def append(replyMsg: Message):ReplyBuilder = {
        _replies.append(replyMsg)
        this
    }

    /**
     * Appends all the elements within the given ReplyBuilder
     * to this and returns this.
     */
    def append(replyBuilder: ReplyBuilder):ReplyBuilder = {
        _replies ++= replyBuilder._replies
        this
    }

    /**
     * Appends the given Reply and returns this.
     */
    def append(reply: Reply):ReplyBuilder = {
        append(new ReplyMessage(dstNick, reply))
    }

    /**
     * Builds and returns a String that represents the messages appended to it.
     * Each new message is prepended with a newline, thus all messages appended
     * will be displayed as separate lines.
     */
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

