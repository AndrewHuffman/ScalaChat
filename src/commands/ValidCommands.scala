package commands

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/24/11
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */

object ValidCommands extends Enumeration {
    type ValidCommands = Value
    val NICK, USER, PING, PONG, UNKNOWN = Value
}