package targets

import db.tables.UserTable
import db.{IRCDB}

import org.squeryl.PrimitiveTypeMode._
import messages.parsers.UserMask
import messages.{Messages, Message}
import collection.mutable.HashSet
import net.{Server, UserConnection}

/**
 * Gives operations that interact with the given user record and connection.
 */
class User(val connection: UserConnection, val record: UserTable) extends Target {
    //Insert user into DB upon creation
    //TODO: Check if user already exists.
    Users.insert(record)

    /**
     * Disconnects the user. Does not provide a quit messsage.
     */
    def quit() {
        connection.disconnect()
    }

    /**
     * Returns the UserMask for this user
     */
    def mask = {
        new UserMask(record.nick, record.user, record.host)
    }

    /**
     * Workflow that will update the record in the server after
     * executing p
     */
    private def update(p: => Unit) {
        p
        Users.update(record)
    }

    /**
     * Updates the record to indicate the user is now registerd
     */
    def register() {
        update {
            record.registered = true
        }
    }

    /**
     * Changes the record to update the given name
     */
    def changeNick(name: String) {
        update {
            record.nick = name
        }
    }

    /**
     * Changes the record to update the given username
     */
    def username(name: String) {
        update {
            record.user = name
        }
    }

    /**
     * Changes the record to update the given realname
     */
    def realname(name: String) {
        update {
            record.real = name
        }
    }

    /**
     * Deletes the record from the UserTable and all associated tables
     * (ChannelTable and ChannelInviteTable)
     */
    def delete() {
        IRCDB.execute {
            println("chan dissociate:" + record.channels.dissociateAll)
            println("invite dissociate: " + record.invites.dissociateAll)
        }
        Users.delete(record.id)
    }

    /**
     * Sends the given String to the user connection.
     */
    def send(out: String) {
        connection.send(out)
    }

    /**
     * Returns an Iterable[ChannelTable] of all the channels
     * the user is within.
     */
    def channels = {
        IRCDB.execute {
            for(chan <- record.channels) yield chan
        }
    }


    /**
     * Note: Does not produce join message, only adds the user to the
     * channel (REVIEW: is this a good idea?)
     */
    def join(chan: Channel) {
        chan.addUser(this)
    }

    /**
     * Note: Does not produce part message, only removes the user from
     * the channel. A Part or Kick message should be sent.
     */
    def part(chan: Channel) {
        chan.removeUser(this)
    }

    /**
     * Returns true if the user is within the given channel,
     * false otherwise.
     */
    def isIn(chan: Channel) = {
        val record = chan.record
        channels.exists(_.id == record.id)
    }

    /**
     * Sends Message to all channels the user is within
     */
    def broadcast(out: Message) {
        broadcast(out.toString)
    }

    /**
     * Sends String to all channels the user is within.
     */
    def broadcast(out: String) {
        uniqueCompanions.foreach(u => {
            u.send(out)
        })
    }

    /**
     * Returns a list of unique Users that are within all the channels this user is in.
     *
     * TODO: It's not even funny how inefficeint this function is
     */
    def uniqueCompanions = {
        val _set = HashSet.empty[UserTable]
        //add the users in all the common channels to a set (removing duplicates)
        for(c <- channels) {
            (new Channel(c)).users.foreach(usr => {
                _set += usr
            })
        }
        //translate each UserTable to a User by finding the UserConnection
        _set.map(record => {
            val conn = Server.getConnection(record.userConnID)
            conn.get.user
        })
    }

    /**
     * Returns true if the user is an operator in the
     * given chanenl, false otherwise.
     */
    def isOpIn(channel: Channel) = {
        //TODO: Complete
        false
    }

}