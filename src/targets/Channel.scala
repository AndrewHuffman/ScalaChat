package targets

import messages.Messages
import net.Server
import db.IRCDB
import db.tables.{UserTable, ChannelTable}
import messages.Message

class Channel(val record: ChannelTable) extends Target {
    def update(p: => Unit) {
        p
        Channels.update(record)
    }

    def users = {
        IRCDB.execute {
            for(u <- record.users) yield u
        }
    }

    def send(out: String) {
        users.foreach(usr => {
            sendToUser(usr, out)
        })
    }

    def sendOmit(msg: Message, user: User) {
        users.foreach(usr => {
            if (usr.id != user.record.id) {
                sendToUser(usr, msg.toString)
            }
        })
    }

    private def sendToUser(usr: UserTable, out: String) {
        val connection = Server.getConnection(usr.userConnID)
        if (connection.isDefined) connection.get.send(out)
    }

    def addUser(user: User) {
        if (!containsUser(user)) {
            IRCDB.execute {
                record.users.associate(user.record)
            }
        }
    }

    def removeUser(user: User) {
        if (containsUser(user)) {
            IRCDB.execute {
                record.users.dissociate(user.record)
            }
        }
    }

    def getTopic = record.topic

    def setTopic(newTopic: String) {
        update {
            record.topic = newTopic
        }
    }

    def containsUser(user: User) = {
        users.exists(_.id == user.record.id)
    }
}