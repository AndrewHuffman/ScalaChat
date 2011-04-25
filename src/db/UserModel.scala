package db

import db._
import org.squeryl.PrimitiveTypeMode._
import messages.parsers.UserMask
import org.squeryl.dsl.ast.UpdateAssignment

object UserModel extends Model[UserTable](IRCDB.users) {
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

    def register(user_id: Long) = execute {
        update(users)(u =>
            where(u.id === user_id)
            set(u.registered := true)
        )
    }

    def delete(user_id: Long) = execute {
        users.deleteWhere(u => u.id === user_id)
    }

    def setHost(user_id: Long, host: String) =
        setById(user_id, (u:UserTable) => u.host := host)

    def setNickname(user_id: Long, nick: String) =
        setById(user_id, (u:UserTable) => u.nick := nick)

    def setUsername(user_id: Long, username: String) =
        setById(user_id, (u:UserTable) => u.user := username)

    def setRealname(user_id: Long, realname: String) =
        setById(user_id, (u:UserTable) => u.real := realname)

    def setById(user_id: Long, setClause: (UserTable) => UpdateAssignment) = execute {
        update(IRCDB.users)(u =>
            where(u.id === user_id)
            set(setClause(u))
        )
    }
}