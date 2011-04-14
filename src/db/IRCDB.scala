package db

import org.squeryl.PrimitiveTypeMode._
import java.sql.Timestamp
import java.util.Date
import org.squeryl.adapters.H2Adapter
import org.squeryl.dsl.{OneToMany, ManyToOne, CompositeKey2}
import org.squeryl._

class ChannelTable(val id: Long, val name : String,
                   val p_mode : Boolean, val s_mode : Boolean,
                   val i_mode : Boolean, val t_mode : Boolean,
                   val n_mode : Boolean, val m_mode : Boolean,
                   val key : Option[String]) extends KeyedEntity[Long] {
    def this() = this(0,"", false, false, false, false, false, false, Some(""))

    lazy val bans:OneToMany[ChannelBanTable] = IRCDB.channelToChanBans.left(this)
    lazy val users = IRCDB.channelUsers.left(this)
    lazy val invites = IRCDB.channelInvites.left(this)
}

class UserTable(val id: Long, val nick : String,
                val user : String, val real: String,
                val host : String, val away : String,
                val i_mode : Boolean, val w_mode : Boolean,
                val s_mode : Boolean, val o_mode : Boolean,
                val signed_on : Timestamp) extends KeyedEntity[Long] {
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

object IRCDB extends Schema {
    val users = table[UserTable]
    val channels = table[ChannelTable]
    val channelBans = table[ChannelBanTable]
    val opers = table[OpersTable]

    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() =>
        Session.create(
            java.sql.DriverManager.
                getConnection("jdbc:h2:~/irc",
                "ScalaIRC",""), new H2Adapter)
    )

    on(users)(u => declare(
        u.i_mode defaultsTo(false),
        u.w_mode defaultsTo(false),
        u.s_mode defaultsTo(false),
        u.o_mode defaultsTo(false),
        u.nick is(unique,indexed)
    ))

    on(channels)(c => declare(
        c.p_mode defaultsTo(false),
        c.s_mode defaultsTo(false),
        c.i_mode defaultsTo(false),
        c.t_mode defaultsTo(false),
        c.n_mode defaultsTo(false),
        c.m_mode defaultsTo(false),
        c.name is(unique, indexed)
    ))

    on(channelBans)(c => declare(
        columns(c.chan_id, c.ban_mask) are(unique)
    ))

    //The Channels and Users tables are connected through a many-to-many relationship
    //using the ChannelUsersTable.
    val channelUsers = manyToManyRelation(channels, users).
        via[ChannelUserTable]((c, u, cu) => (cu.chan_id === c.id, u.id === cu.user_id))

    val channelInvites = manyToManyRelation(channels, users).
        via[ChannelInviteTable]((c, u, cu) => ((cu.chan_id === c.id), (u.id === cu.user_id)))

    val channelToChanBans = oneToManyRelation(channels, channelBans).
        via((c, cb) => c.id === cb.id)

    def execute[T](query: => T):T = {
        transaction {
            query
        }
    }

    def getAllChannels = {
        val query = from(channels)(c => select(c))
        transaction { for(z <- query) yield z }
    }
}

