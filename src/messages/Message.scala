package messages

import parsers._
import targets.User

class Message(val prefix:Option[String], val command:String, val params:String = "") {
    def this(prefix: String, command: String, params: Params) = this(Some(prefix),command, params.toString)
    def this(command: String, params: Params) = this(None, command, params.toString)

    override def toString = {
        val sb = new StringBuilder
        if (prefix.isDefined) sb.append(":").append(prefix.get).append(" ")
        sb.append(command).append(" ").append(params)
        sb.toString()
    }
}

class UserMessage(val user: User, command :String, params : Option[Params])
    extends Message(command, params.getOrElse{Params("")})

object Messages {
    case class JoinMessage(channel: String, user: User) extends Message(Some(user.mask.toString),"JOIN", ":"+channel)
    case class PartMessage(channel: String, user: User) extends Message(Some(user.mask.toString),"PART", ":"+channel)
    case class PrivateMessage(target: String, user: User, msg:String) extends Message(Some(user.mask.toString),"PRIVMSG", target+ " :" + msg)
    case class NickMessage(newNick: String, user: User) extends Message(Some(user.mask.toString), "NICK", ":" + newNick)
    case class QuitMessage(msg: String, user: User) extends Message(Some(user.mask.toString), "QUIT", ":"+msg)
    case class TopicMessage(topic: String, chan: String, user: User) extends Message(Some(user.mask.toString), "TOPIC", chan + " :" + topic)
}