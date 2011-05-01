package targets

import db.tables.UserTable
import db.{IRCDB}

import org.squeryl.PrimitiveTypeMode._
import messages.parsers.UserMask
import messages.{Messages, Message}
import collection.mutable.HashSet
import net.{Server, UserConnection}

class User(val connection: UserConnection, val record: UserTable) extends Target {
    //val users = IRCDB.users
    Users.insert(record)

    def quit() {
        connection.disconnect()
    }

    def mask = {
        new UserMask(record.nick, record.user, record.host)
    }

    def update(p: => Unit) {
        p
        Users.update(record)
    }

    def register() {
        update {
            record.registered = true
        }
    }

    def changeNick(name: String) {
        update {
            record.nick = name
        }
    }

    def username(name: String) {
        update {
            record.user = name
        }
    }

    def realname(name: String) {
        update {
            record.real = name
        }
    }

    def delete() {
        IRCDB.execute {
            println("chan dissociate:" + record.channels.dissociateAll)
            println("invite dissociate: " + record.invites.dissociateAll)
        }
        Users.delete(record.id)
    }

    def send(out: String) {
        connection.send(out)
    }

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

    def isOpIn(channel: Channel) = {
        //TODO: Complete
        false
    }

}