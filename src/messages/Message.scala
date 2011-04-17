package messages

import parsers._
import db.User
import commands.{ReplyBuilder}

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/23/11
 * Time: 7:27 PM
 * To change this template use File | Settings | File Templates.
 */

class Message(val user:User, val prefix :Option[Prefix], val command :String, val params :Params) {
    def this(data: MessageData) = this(data.user, data.prefix, data.command, data.params.getOrElse(Params("")))

    //def apply()
    //def unapply()
}