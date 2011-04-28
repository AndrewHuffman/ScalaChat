package messages.parsers

import db.{User, UserModel}

abstract class Tokens
abstract class Prefix extends Tokens
case class ServerName(server: HostName) extends Prefix {
    override def toString = (new StringBuilder(":")).append(server.toString).toString()
}
case class UserMask(nick: Option[NickName], user: Option[UserName], host: Option[HostName]) extends Prefix {
    override def toString = {
        val sb = new StringBuilder(":")
        sb.append(nick.getOrElse { NickName("*") })
        sb.append("!")
        sb.append(user.getOrElse { UserName("*") })
        sb.append("@")
        sb.append(host.getOrElse { HostName("*") })
        sb.toString()
    }
}
case class Params(params: String) extends Tokens {
    override def toString = params
}

case class NickName(name:String) extends Tokens {
    def inUse = {
        UserModel.exists(name)
    }

    override def toString = name
}
case class ChannelName(name:String) extends Tokens {
    override def toString = name
}
case class HostName(host:String) extends Tokens {
    override def toString = host
}
case class UserName(name:String) extends Tokens {
    override def toString = name
}
case class Tail(tail: String) extends Tokens {
    override def toString = tail
}