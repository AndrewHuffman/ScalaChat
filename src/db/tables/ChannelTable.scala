package db.tables

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import java.util.Date
import org.squeryl.dsl.{ManyToOne, CompositeKey2}
import org.squeryl.PrimitiveTypeMode._
import db.IRCDB

class ChannelTable(val id: Long = 0, val name : String, var topic : String = "",
                   var p_mode : Boolean = false, var s_mode : Boolean = false,
                   var i_mode : Boolean = false, var t_mode : Boolean = false,
                   var n_mode : Boolean = false, var m_mode : Boolean = false,
                   var key : Option[String] = None) extends KeyedEntity[Long] {
    def this() = this(0,"", "", false, false, false, false, false, false, Some(""))

    lazy val bans = IRCDB.channelToChanBans.left(this)
    lazy val users = IRCDB.channelUsers.left(this)
    lazy val invites = IRCDB.channelInvites.left(this)
}







