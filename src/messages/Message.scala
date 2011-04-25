package messages

import parsers._
import db.User

class Message(val user:User, val prefix :Option[Prefix], val command :String, val params :Params) {
    def this(data: MessageData) = this(data.user, data.prefix, data.command, data.params.getOrElse(Params("")))

    //def apply()
    //def unapply()
}