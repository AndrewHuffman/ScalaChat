package messages

import messages._
import parsers.{CommandParser, Params}
import db.User
import collection.mutable.ArrayBuffer

//TODO: I dislike this class. The method with communicating to clients needs to be overhauled
class ReplyBuilder(dstNick: String, reply:Reply, params: String*) {
    //val nickname = user.record.
    val _replies = new ArrayBuffer[(Reply, Array[String])]
    _replies.append((reply,params.toArray))

    def append(reply: Reply, params: Array[String]):ReplyBuilder = {
        _replies.append((reply,params))
        this
    }

    def append(reply: Reply, params: String*):ReplyBuilder = {
        append(reply, params.toArray)
    }

    def append(replyBuilder: ReplyBuilder):ReplyBuilder = {
        _replies ++= replyBuilder._replies
        this
    }

    def get:String = {
        val sb = new StringBuilder

        _replies.foreach ((t) => {
            val reply = t._1
            val params = t._2
            if (reply != Reply.RPL_NONE) {
                sb.append(MessageBuilder.create("127.0.0.1", reply.getCode.toString, reply.createMessage(params)))
                sb.append("\n")
            }
        })

        sb.toString()
    }

    def getCode:Int = {
        reply.getCode
    }
}

//just to get IntelliJ to shut up






//TODO: Investigate whether or not I actually need to separate
//parameters from the command.



