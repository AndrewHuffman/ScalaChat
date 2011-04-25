package db

import net.UserConnection

class User(val connection: UserConnection, val id: Long) {
    //TODO: Bad design, should be checking db
    var hasSetNick = false
    var hasSetUser = false
    var nickCache = ""

    def record = {
        //TODO: Handle using getOrElse
        UserModel.get(id).get
    }

    def nick(name: String) = {
        nickCache = name;
        UserModel.setNickname(id, name)
    }
    def username(name: String) = UserModel.setUsername(id, name)
    def realname(name: String) = UserModel.setRealname(id, name)
    def host(host: String) = UserModel.setHost(id, host)

    def send(out: String) {
        connection.send(out)
    }
}