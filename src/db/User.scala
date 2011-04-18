package db

import net.UserConnection

class User(val connection: UserConnection, val id: Long) {
    var hasSetNick = false
    var hasSetUser = false

    def record = {
        //TODO: Handle using getOrElse
        UserModel.get(id).get
    }

    def nick(name: String) = UserModel.setNick(id, name)
}