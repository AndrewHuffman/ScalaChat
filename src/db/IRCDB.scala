package db

import org.squeryl.PrimitiveTypeMode._
import java.sql.Timestamp
import java.util.Date
import org.squeryl.adapters.H2Adapter
import org.squeryl.dsl.{OneToMany, ManyToOne, CompositeKey2}
import org.squeryl._



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
        u.registered defaultsTo(false),
        u.nick is(unique,indexed)
    ))

    on(channels)(c => declare(
        c.n_mode defaultsTo(false),
        c.p_mode defaultsTo(false),
        c.s_mode defaultsTo(false),
        c.i_mode defaultsTo(false),
        c.t_mode defaultsTo(false),
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

