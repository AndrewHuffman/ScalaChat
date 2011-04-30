package targets

import net.UserConnection
import db.tables.UserTable
import db.{IRCDB}

import org.squeryl.PrimitiveTypeMode._
import messages.parsers.UserMask
import messages.{Messages, Message}

class User(val connection: UserConnection, val record: UserTable) extends Target {
    //val users = IRCDB.users
    Users.insert(record)

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
        channels.foreach(c => {
            (new Channel(c)).send(out)
        })
    }
}