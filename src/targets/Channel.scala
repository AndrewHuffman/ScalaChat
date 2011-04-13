package targets

import collection.mutable.ArrayBuffer

class Channel(val name: String) {
    var users = new ArrayBuffer[User]()

    def sendMsg(msg: String) {
        users.foreach((usr) => {
            usr.sendMsg(msg)
        })
    }

    def userJoin(user: User) {
        users.append(user)
    }

    def userPart(user: User) {
        val idx = users.indexOf(user)
        if (idx > 0) users.remove(idx)
    }

    def getName() = name
}
