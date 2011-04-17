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

    def get(id: Long):Option[UserTable] = {
        IRCDB.users.lookup(id)
    }

    def get(mask: UserMask):Option[UserTable] = {
        None
    }

    def register(user_id: Long) {
        update(IRCDB.users)(u =>
            where(u.id === user_id)
            set(u.registered := true)
        )
    }
}