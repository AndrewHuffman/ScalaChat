package db

import net.UserConnection

class User(val connection: UserConnection, val id: Long) {
    var hasSetNick = false
    var hasSetUser = false

    def record = UserModel.get(id).get //TODO: Handle using getOrElse
}