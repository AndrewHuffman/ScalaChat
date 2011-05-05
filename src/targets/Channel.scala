package targets

import messages.Messages
import net.Server
import db.IRCDB
import db.tables.{UserTable, ChannelTable}
import messages.Message

/**
 * Gives actions for the given ChannelTable record
 */
class Channel(val record: ChannelTable) extends Target {
    /**
     * Deletes the channel from the DB
     */
    def update(p: => Unit) {
        p
        Channels.update(record)
    }

    /**
     * Retruns an Iterable[UserTable] of all the
     * users within the channel.
     */
    def users = {
        IRCDB.execute {
            for(u <- record.users) yield u
        }
    }

    /**
     * Returns the number of users within the channel
     */
    def numUsers = users.size

    /**
     * Sends a mesage to all the users within the table.
     */
    def send(out: String) {
        users.foreach(usr => {
            sendToUser(usr, out)
        })
    }

    /**
     * Sends a message to all the suers within the table,
     * omitting the given user.
     *
     * TODO: support multiple users.
     */
    def sendOmit(msg: Message, user: User) {
        users.foreach(usr => {
            if (usr.id != user.record.id) {
                sendToUser(usr, msg.toString)
            }
        })
    }

    /**
     * Send a string to the given UserTable.
     */
    private def sendToUser(usr: UserTable, out: String) {
        val connection = Server.getConnection(usr.userConnID)
        if (connection.isDefined) connection.get.send(out)
    }

    /**
     * Adds the given user to this channel.
     */
    def addUser(user: User) {
        if (!containsUser(user)) {
            IRCDB.execute {
                record.users.associate(user.record)
            }
        }
    }

    /**
     * Removes the given user from this chanenl.
     */
    def removeUser(user: User) {
        if (containsUser(user)) {
            IRCDB.execute {
                record.users.dissociate(user.record)
            }
        }
    }

    /**
     * Returns the topic set within this channel
     */
    def getTopic = record.topic

    /**
     * Sets topic of this channel to the given string
     */
    def setTopic(newTopic: String) {
        update {
            record.topic = newTopic
        }
    }

    /**
     * Returns true if the suer is within the channel, false
     * otherwise.
     */
    def containsUser(user: User) = {
        users.exists(_.id == user.record.id)
    }
}