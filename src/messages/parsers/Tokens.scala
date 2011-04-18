package messages.parsers

import db.{User, UserModel}

abstract class Tokens
abstract class Prefix extends Tokens
case class ServerName(server: HostName) extends Prefix
case class UserMask(nick: Option[NickName], user: Option[UserName], host: Option[HostName]) extends Prefix
case class Params(params: String) extends Tokens
case class MessageData(user:User, prefix:Option[Prefix], command:String, params:Option[Params]) extends Tokens
case class NickName(name:String) extends Tokens {
    def inUse = {
        UserModel.exists(name)
    }
}
case class ChannelName(name:String) extends Tokens
case class HostName(host:String) extends Tokens
case class UserName(name:String) extends Tokens
case class Tail(tail: String) extends Tokens