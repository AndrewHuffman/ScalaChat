package targets

import net.UserConnection
import messages.Success

/**
* Not thread safe.
*/
class User(connection: UserConnection) {
    private var nick:String = ""
    private var user:String = ""
    private var real:String = ""
    private var away:String = ""

    val isRegistered = (!nick.isEmpty && !user.isEmpty && !real.isEmpty)
    val isAway = !away.isEmpty

    def registerUser(user:String, real:String) = {
        this.user = user
        this.real = real
        //TODO: error check
        Success((user, real));
    }

    def setNick(nick:String) = {
        this.nick = nick
        //TODO: error check
        Success(nick);
    }

    def setAway(away: String) {
        this.away = away;
    }

    def sendMsg(msg: CharSequence) {
        connection.sendMsg(msg);
    }

    def getName() = {
        nick
    }
}