package db

import db._
import org.squeryl.PrimitiveTypeMode._
import messages.parsers.UserMask

object UserModel extends Model[UserTable](IRCDB.users) {

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
            IRCDB.users.lookup(id)
        }
    }

    def get(mask: UserMask):Option[UserTable] = execute { None }

    def register(user_id: Long) = execute {
        update(IRCDB.users)(u =>
            where(u.id === user_id)
            set(u.registered := true)
        )
    }

    def setNick(user_id: Long, newNick: String) = execute {
        update(IRCDB.users)(u =>
            where(u.id === user_id)
            set(u.nick := newNick)
        )
    }
}