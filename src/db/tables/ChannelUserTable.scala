package db.tables

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import java.util.Date
import org.squeryl.dsl.{ManyToOne, CompositeKey2}
import org.squeryl.PrimitiveTypeMode._

class ChannelUserTable(val chan_id : Long,
                       val user_id : Long,
                       var o_mode : Boolean = false,
                       var v_mode : Boolean = false)
    extends KeyedEntity[CompositeKey2[Long, Long]] {

    def id = compositeKey(chan_id, user_id)
}


