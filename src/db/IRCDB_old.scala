//package db
//
//import org.scalaquery.session._
//import org.scalaquery.session.Database.threadLocalSession
//import org.scalaquery.ql.extended.{ExtendedTable => Table}
//import org.scalaquery.ql.extended.H2Driver.Implicit._
//import org.scalaquery.ql.{Column, Query}
//
//object Channels extends Table[(Int, String, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String)]("channels") {
//    def id = column[Int]("chan_id", O PrimaryKey)
//    def name = column[String]("name", O NotNull)
//    def p_mode = column[Boolean]("p_mode", O Default false)
//    def s_mode = column[Boolean]("s_mode", O Default false)
//    def i_mode = column[Boolean]("i_mode", O Default false)
//    def t_mode = column[Boolean]("t_mode", O Default false)
//    def n_mode = column[Boolean]("n_mode", O Default false)
//    def m_mode = column[Boolean]("m_node", O Default false)
//    def key = column[String]("key")
//    def * = id ~ name ~ p_mode ~ s_mode ~i_mode ~ t_mode ~ n_mode ~ m_mode ~ key
//}
//
//object Users extends Table[(Int, String, String, String, String, String, Boolean, Boolean, Boolean, Boolean)]("users") {
//    def id = column[Int]("user_id", O PrimaryKey, O AutoInc)
//    def nick = column[String]("nick", O NotNull)
//    def user = column[String]("user", O NotNull)
//    def real = column[String]("real", O NotNull)
//    def host = column[String]("host", O NotNull)
//    def away = column[String]("away", O NotNull)
//    def i_mode = column[Boolean]("i_mode", O Default false)
//    def w_mode = column[Boolean]("w_mode", O Default false)
//    def s_mode = column[Boolean]("s_mode", O Default false)
//    def o_mode = column[Boolean]("o_mode", O Default false)
//    def * = id ~ nick ~ user ~ real ~ host ~ away ~ i_mode ~ w_mode ~ s_mode ~ o_mode
//}
//
//object ChannelUsers extends Table[(Int, Int, Int)]("channel_users") {
//    def id = column[Int]("chan_users_id", O PrimaryKey, O AutoInc)
//    def chan_id = column[Int]("chan_id", O NotNull)
//    def user_id = column[Int]("user_id", O NotNull)
//    def * = id ~ chan_id ~ user_id
//    //The following is only supported in ScalaQuery 0.9.2:
//    //def pk = primaryKey("pk_channel_users", chan_id ~ user_id)
//    def fk_chan_id = foreignKey("fk_chanuser_chan_id", chan_id, Channels)(_.id)
//    def fk_user_id = foreignKey("fk_chanuser_user_id", user_id, Users)(_.id)
//    //def channelJoin = Channels.wh
//}
//
//object ChannelBan extends Table[(Int, Int, String)]("channel_bans") {
//    def id = column[Int]("chan_ban_id", O PrimaryKey, O AutoInc)
//    def chan_id = column[Int]("chan_id", O NotNull)
//    def ban_mask = column[String]("ban_mask", O NotNull)
//    def * = id ~ chan_id ~ ban_mask
//    //The following is only supported in ScalaQuery 0.9.2:
//    //def pk = primaryKey("pk_chan_bans", chan_id ~ ban_mask)
//    def fk_chan_id = foreignKey("fk_chanban_chan_id", chan_id, Channels)(_.id)
//}
//
//object ChannelInvites extends Table[(Int, Int, Int)]("channel_invites") {
//    def id = column[Int]("chan_invites_id", O PrimaryKey, O AutoInc)
//    def chan_id = column[Int]("chan_id", O NotNull)
//    def user_id = column[Int]("user_id", O NotNull)
//    def * = id ~ chan_id ~ user_id
//    //The following is only supported in ScalaQuery 0.9.2:
//    //def pk = primaryKey("pk_chan_invites", chan_id ~ user_id)
//    def fk_chan_id = foreignKey("fk_chaninvite_chan_id", chan_id, Channels)(_.id)
//    def fk_user_id = foreignKey("fk_chaninvite_user_id", user_id, Users)(_.id)
//}
//
//object Opers extends Table[(Int, String, String)]("opers") {
//    def id = column[Int]("id", O PrimaryKey, O AutoInc)
//    def mask = column[String]("mask", O NotNull)
//    def password = column[String]("password", O NotNull)
//    def * = id ~ mask ~ password
//}
//
//object TestDB {
//    def create = session {
//        val ddl = (Channels.ddl ++ Users.ddl ++ ChannelUsers.ddl ++ ChannelBan.ddl ++ ChannelInvites.ddl ++ Opers.ddl)
//        ddl.drop
//        ddl.create
//    }
//
//    def insertTestUser(nick:String = "myNewNickName", user:String = "myNewUser", real: String = "this is my real name!", host: String = "127.0.0.1", away: String = "new away message") = {
//        session {
//            Users.nick ~ Users.user ~ Users.real ~ Users.host ~ Users.away insert(
//                (nick, user, real, host, away)
//                )
//        }
//    }
//
//    def getAll() = {
//        val qall = for (e <- Users) yield e
//        session {
//            val list = qall.list
//            qall foreach println
//            qall
//        }
//    }
//
//    def session[T](p: => T) = {
//        val db = Database.forURL(
//            url = "jdbc:h2:~/irc",
//            driver = "org.h2.Driver",
//            user = "ScalaIRC"
//        )
//
//        db.withSession{
//            p
//        }
//    }
//}
//
//class Model {
//
//    def session[T](p: => T) = {
//        val db = Database.forURL(
//            url = "jdbc:h2:~/irc",
//            driver = "org.h2.Driver",
//            user = "ScalaIRC"
//        )
//
//        db.withSession{
//            p
//        }
//    }
//}

//class Channels extends Model {
//    def getChannels = {
//        val q = for (c <- Channels) yield c
//        session {
//            q.list
//        }
//    }
//
//    def get(name : String) = {
//        val q = for (c <- Channels if c.name === name) yield c
//        session {
//            q.first
//        }
//    }
//
//    def create(name : String) = session {
//        Channels.name.insert(name.toLowerCase)
//    }
//
//    def delete(q: Query[Channels.type]) = session {
//        q.delete
//    }
//
//    def deleteWhere[T, B](f: Channels => Boolean) = session {
//        delete (for(c <- Channels if f(c)) yield c)
//    }
//
//    def destroy(id : Int) = {
//        //val q = for (c <- Channels if c.name === name) yield c
//        //delete q
//        deleteWhere(_.id === id)
//    }
//
//    def destroy(name : String) = {
////        val q = for (c <- Channels if c.name === name.toLowerCase) yield c
////        delete q
//        delteWhere(_.name === name.toLowerCase)
//    }
//}