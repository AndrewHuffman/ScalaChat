package messages.parsers

import targets.{Channel, Channels, Users, User}
import net.Server

abstract class Tokens
abstract class Prefix extends Tokens
case class ServerName(server: HostName) extends Prefix {
    override def toString = (new StringBuilder(":")).append(server.toString).toString()
}
case class UserMask(nick: Option[NickName], user: Option[UserName], host: Option[HostName]) extends Prefix {
    def this(nick: String, user: String, host: String) = this(Some(NickName(nick)),Some(UserName(user)),Some(HostName(host)))

    override def toString = {
        val sb = new StringBuilder
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

class Receiver extends Tokens

case class NickName(name:String) extends Receiver {
    /**
     * Returns true if a use with this nickname is connected
     * to the server.
     */
    def exists = {
        Users.exists(name)
    }

    override def toString = name


    /**
     * If a user with this nick name exists, it will return
     * a Some containing the User object. Otherwise it will
     * return None
     */
    def getUser:Option[User] = {
        Users.get(name) match {
            case Some(record) => {
                Some(Server.getConnection(record.userConnID).get.user)
            }
            case None => None
        }
    }
}
case class ChannelName(name:String) extends Receiver {
    override def toString = name

    def exists = Channels.exists(name)

    def getRecord = {
        Channels.get(name)
    }

    def getChannel = {
        getRecord match {
            case Some(record) => Some(new Channel(record))
            case None => None
        }
    }
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