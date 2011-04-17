package db

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import java.util.Date
import org.squeryl.dsl.{ManyToOne, CompositeKey2}
import org.squeryl.PrimitiveTypeMode._

object Tables //Might add to this, but otherwise here to shut IntelliJ up.

class ChannelTable(val id: Long = 0, val name : String,
                   val p_mode : Boolean = false, val s_mode : Boolean = false,
                   val i_mode : Boolean = false, val t_mode : Boolean = false,
                   val n_mode : Boolean = false, val m_mode : Boolean = false,
                   val key : Option[String] = None) extends KeyedEntity[Long] {
    def this() = this(0,"", false, false, false, false, false, false, Some(""))

    lazy val bans = IRCDB.channelToChanBans.left(this)
    lazy val users = IRCDB.channelUsers.left(this)
    lazy val invites = IRCDB.channelInvites.left(this)
}
//TODO: Last activity - for zee pings!
class UserTable(val id: Long = 0, val nick : String,
                val user : String = "", val real: String = "",
                val host : String = "", val away : String = "",
                val i_mode : Boolean = false, val w_mode : Boolean = false,
                val s_mode : Boolean = false, val o_mode : Boolean = false,
                val signed_on : Timestamp = new Timestamp((new Date).getTime),
                val registered: Boolean = false) extends KeyedEntity[Long] {
    def this() = this(0, "", "", "", "", "",
        false, false, false, false,
        new Timestamp((new Date()).getTime))

    lazy val channels = IRCDB.channelUsers.right(this)
    lazy val invites = IRCDB.channelInvites.right(this)
}

class ChannelUserTable(val chan_id : Long,
                       val user_id : Long) extends KeyedEntity[CompositeKey2[Long, Long]] {
    def id = compositeKey(chan_id, user_id)
}

class ChannelBanTable(val id : Long, val chan_id : Long,
                      val ban_mask : String) extends KeyedEntity[Long] {
    lazy val channels: ManyToOne[ChannelTable] = IRCDB.channelToChanBans.right(this)
}

class ChannelInviteTable(val chan_id : Long,
                         val user_id : Long) extends KeyedEntity[CompositeKey2[Long, Long]] {

    def id = compositeKey(chan_id, user_id)
}

class OpersTable(val id : Long, val mask : String, val password : String) extends KeyedEntity[Long] {
    def this() = this(0,"*","")
}