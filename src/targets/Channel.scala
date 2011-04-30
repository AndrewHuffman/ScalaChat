package targets

import db.tables.ChannelTable
import messages.Messages
import net.Server
import db.IRCDB

class Channel(val record: ChannelTable) extends Target {
    def users = {
        IRCDB.execute {
            for(u <- record.users) yield u
        }

    }

    def send(out: String) {
        users.foreach(usr => {
            val connection = Server.getConnection(usr.userConnID)
            if (connection.isDefined) connection.get.send(out)
        })
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

    def containsUser(user: User) = {
        users.exists(_.id == user.record.id)
    }
}