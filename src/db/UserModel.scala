package db

import db._
import org.squeryl.PrimitiveTypeMode._

object UserModel extends Model[UserTable](IRCDB.users) {
    def get(nick: String) = {
        getWhereFirst(user => user.nick === nick)
    }

    def get(id: Long) = {
        IRCDB.users.lookup(id)
    }

    def get(mask: UserMask) = {
        None
    }
}

class UserMask(mask :String) {

}