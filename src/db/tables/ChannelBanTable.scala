package db.tables

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import java.util.Date
import org.squeryl.dsl.{ManyToOne, CompositeKey2}
import org.squeryl.PrimitiveTypeMode._
import db.IRCDB

class ChannelBanTable(val id : Long, val chan_id : Long,
                      val ban_mask : String) extends KeyedEntity[Long] {
    lazy val channels: ManyToOne[ChannelTable] = IRCDB.channelToChanBans.right(this)
}
