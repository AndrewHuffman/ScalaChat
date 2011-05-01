package targets

import db._
import org.squeryl.PrimitiveTypeMode._
import messages.parsers.UserMask
import tables.UserTable

object Users extends Model[UserTable](IRCDB.users) {
    val users = table

    def get(nick: String):Option[UserTable] = {
        getWhereFirst(user => user.nick === nick)
    }

    def exists(nick: String) = {
        get(nick) match {
            case Some(n) => true
            case None => false
        }
    }

    def get(id: Long) = {
        execute {
            table.lookup(id)
        }
    }

    def get(mask: UserMask):Option[UserTable] = execute { None }

    def delete(user_id: Long) = {
        execute {
            println("user delete:" + users.deleteWhere(_.id === user_id))
        }
    }
}