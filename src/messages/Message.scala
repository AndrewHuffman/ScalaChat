package messages

import parsers._
import targets.User

/**
 * Represents a Message that can be sent to or from a User or Server
 *
 * Messages are formatted as such:
 *  :prefix COMMAND PARAM1 PARAM2 :TAIL MESSAGE
 * This object accepts Option[String] or String for the prefix,
 * a command String, and a Param or String for the params.
 *
 * TODO: Refactor and clean up
 */
class Message(val prefix:Option[String], val command:String, val params:String = "") {
    def this(prefix: String, command: String, params: Params) = this(Some(prefix),command, params.toString)
    def this(command: String, params: Params) = this(None, command, params.toString)

    /**
     * Converts the Message object into a String representing it.
     * The string will have the following format:
     *  [:PREFIX] COMMAND [PARAM, ... ] [:TAIL]
     * Where [']'s are optional, and ... represents repetition
     */
    override def toString = {
        val sb = new StringBuilder
        if (prefix.isDefined) sb.append(":").append(prefix.get).append(" ")
        sb.append(command).append(" ").append(params)
        sb.toString()
    }
}

/**
 * A message that originated from a user. This is generated
 * by the Parser
 *
 * user - the user issuing the message
 * command - the command string
 * params - An Option that may contain paramters, if any.
 */
class UserMessage(val user: User, command :String, params : Option[Params])
    extends Message(command, params.getOrElse{Params("")})

/**
 * Container object for the Message case classes that can be used when
 * broadcasting messages from commands.
 */
object Messages {
    case class JoinMessage(channel: String, user: User) extends Message(Some(user.mask.toString),"JOIN", ":"+channel)
    case class PartMessage(channel: String, user: User) extends Message(Some(user.mask.toString),"PART", ":"+channel)
    case class PrivateMessage(target: String, user: User, msg:String) extends Message(Some(user.mask.toString),"PRIVMSG", target+ " :" + msg)
    case class NickMessage(newNick: String, user: User) extends Message(Some(user.mask.toString), "NICK", ":" + newNick)
    case class QuitMessage(msg: String, user: User) extends Message(Some(user.mask.toString), "QUIT", ":"+msg)
    case class TopicMessage(topic: String, chan: String, user: User) extends Message(Some(user.mask.toString), "TOPIC", chan + " :" + topic)
    case class PongMessage(tailStr: String) extends Message("my.server.com","PONG", Params(":"+tailStr))
}