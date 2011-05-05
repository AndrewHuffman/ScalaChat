package messages.parsers

import targets.{Channel, Channels, Users, User}
import net.Server

/**
 * Tokens are case classes that are
 * created by the parser for use in structured
 * messages.
 */
abstract class Tokens
abstract class Prefix extends Tokens

/**
 * Container class for parser output containg the host for the server.
 */
case class ServerName(server: HostName) extends Prefix {
    override def toString = (new StringBuilder(":")).append(server.toString).toString()
}

/**
 * Token that represents a UserMask.
 * UserMasks may have the following format:
 * nick!user@host - if nick, user, or host are not provided, a * is valid in its place.
 * An * represents a wildcard.
 *
 * nick - an Option containing a NickName that contains the nick string
 * user - an Option containing a User object that contains the user string
 * host - an Option containing a HostName object that contains the host string
 *
 */
case class UserMask(nick: Option[NickName], user: Option[UserName], host: Option[HostName]) extends Prefix {
    //TODO: Handle "" better
    def this(nick: String, user: String, host: String) = this(Some(NickName(nick)),Some(UserName(user)),Some(HostName(host)))

    /**
     * Converts the UserMask into its String representation.
     * For each None, an '*' is used in its place.
     */
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

/**
 * Container class for parser output containing the paramter.
 */
case class Params(params: String) extends Tokens {
    override def toString = params
}

/**
 * NickName's and Channels (and in the future, other Servers) that
 * may recieve messages
 */
class Receiver extends Tokens

/**
 * Container class for parser output containing a user's nickname.
 */
case class NickName(name:String) extends Receiver {
    /**
     * Returns true if a use with this nickname is connected
     * to the server.
     */
    def exists = {
        Users.exists(name)
    }

    /**
     * Returns the name.
     */
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
/**
 * Container class for parser output containing a ChannelName.
 *
 * Provides functionality to enable retreiving the DB
 * record of a channel with the same name.
 *
 * name - the channel's name
 */
case class ChannelName(name:String) extends Receiver {
    /**
     * Returns name
     */
    override def toString = name

    /**
     * Returns true if the channel exists, false otherwise
     */
    def exists = Channels.exists(name)

    /**
     * Returns an Option that will contian the channel's record in the
     * database, or None
     */
    def getRecord = {
        Channels.get(name)
    }

    /**
     * Returns an Option containing a Channel object of the
     * channel with the given name, or None if no such
     * channel exists.
     */
    def getChannel = {
        getRecord match {
            case Some(record) => Some(new Channel(record))
            case None => None
        }
    }
}
/**
 * Container class for parser output containing the hostname.
 */
case class HostName(host:String) extends Tokens {
    override def toString = host
}
/**
 * Container class for parser output containing the username
 */
case class UserName(name:String) extends Tokens {
    override def toString = name
}
/**
 * Container class for parser output containing the tail
 */
case class Tail(tail: String) extends Tokens {
    override def toString = tail
}