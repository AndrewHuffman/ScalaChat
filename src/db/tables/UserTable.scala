package db.tables

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import org.squeryl.dsl.{ManyToOne, CompositeKey2}
import org.squeryl.PrimitiveTypeMode._
import db.IRCDB
import java.util.{UUID, Date}

/**
 * User Table
 *
 * Schema table for the users within the database.
 * Joins with ChannelUsres and ChannelInvites
 * representing 2 Many-to-Many relationships with
 * Channels for the users within a channel and users
 * invited into a channel.
 */
class UserTable(val id: Long = 0, var userConnID: String, var nick : String = "",
                var user : String = "", var real: String = "",
                val host : String = "", var away : String = "",
                var i_mode : Boolean = false, var w_mode : Boolean = false,
                var s_mode : Boolean = false, var o_mode : Boolean = false,
                val signed_on : Timestamp = new Timestamp((new Date).getTime),
                var registered: Boolean = false) extends KeyedEntity[Long] {
    def this() = this(0, null, "", "", "", "", "",
        false, false, false, false,
        new Timestamp((new Date()).getTime))

    lazy val channels = IRCDB.channelUsers.right(this)
    lazy val invites = IRCDB.channelInvites.right(this)
}
