package db.tables

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import java.util.Date
import org.squeryl.dsl.{ManyToOne, CompositeKey2}
import org.squeryl.PrimitiveTypeMode._

class OpersTable(val id : Long, val mask : String, val password : String) extends KeyedEntity[Long] {
    def this() = this(0,"*","")
}



