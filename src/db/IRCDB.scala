package db

import org.squeryl.PrimitiveTypeMode._
import java.sql.Timestamp
import java.util.Date
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.dsl.{OneToMany, ManyToOne, CompositeKey2}
import org.squeryl._
import tables._

/***
 * Defines the Database Schema and relations
 * Also provides initialization of the database
 * and the execution thread.
 */
object IRCDB extends Schema {
    val users = table[UserTable]
    val channels = table[ChannelTable]
    val channelBans = table[ChannelBanTable]
    val opers = table[OpersTable]

    /***TABLE RELATIONSHIPS**/

    //The Channels and Users tables are connected through a many-to-many relationship
    //using the ChannelUsersTable.
    val channelUsers = manyToManyRelation(channels, users).via[ChannelUserTable](
        (c, u, cu) => (cu.chan_id === c.id, u.id === cu.user_id)
    )

    val channelInvites = manyToManyRelation(channels, users).via[ChannelInviteTable](
        (c, u, cu) => ((cu.chan_id === c.id), (u.id === cu.user_id))
    )

    //One to Many relationship for channels to channelBans (many bans/channel)
    val channelToChanBans = oneToManyRelation(channels, channelBans).
        via((c, cb) => c.id === cb.id)

    def init {
        /* Connect to database using JDBC */
        Class.forName("org.postgresql.Driver")
        SessionFactory.concreteFactory = Some(() =>
            Session.create(
                java.sql.DriverManager.
                    //getConnection("jdbc:h2:tcp://localhost/~/test","sa",""), new H2Adapter)
                    getConnection("jdbc:postgresql:irc","postgres","mahuff"), new PostgreSqlAdapter)
        )
        /* Define UserTable constraints */
        on(users)(u => declare(
            u.i_mode defaultsTo(false),
            u.w_mode defaultsTo(false),
            u.s_mode defaultsTo(false),
            u.o_mode defaultsTo(false),
            u.registered defaultsTo(false),
            u.nick is(unique,indexed)
        ))


        /* Define ChannelTable constraints */
        on(channels)(c => declare(
            c.n_mode defaultsTo(false),
            c.p_mode defaultsTo(false),
            c.s_mode defaultsTo(false),
            c.i_mode defaultsTo(false),
            c.t_mode defaultsTo(false),
            c.m_mode defaultsTo(false),
            c.name is(unique, indexed)
        ))

        /* Define ChannelBanTable constraints */
        on(channelBans)(c => declare(
            columns(c.chan_id, c.ban_mask) are(unique)
        ))

        def alwaysTrue = true === true
        transaction {
            drop
            create
            channelBans.deleteWhere(r => alwaysTrue)
            channelUsers.deleteWhere(r => alwaysTrue)
            channelInvites.deleteWhere(r => alwaysTrue)
            users.deleteWhere(r => alwaysTrue)
            channels.deleteWhere(r => alwaysTrue)
        }
    }

    def execute[T](query: => T):T = transaction {
        query
    }
}

