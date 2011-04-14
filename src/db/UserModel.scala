package db

import db._
import org.squeryl.PrimitiveTypeMode._
import messages.parsers.UserMask

object UserModel extends Model[UserTable](IRCDB.users) {
    def get(nick: String) = {
        getWhereFirst(user => user.nick === nick)
    }

    def exists(nick: String) = {
        get(nick) match {
            case Some(n) => true
            case None => false
        }
    }

    def get(id: Long) = {
        IRCDB.users.lookup(id)
    }

    def get(mask: UserMask) = {
        None
    }
}