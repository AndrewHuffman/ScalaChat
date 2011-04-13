package commands

import messages._

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/24/11
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */

class SetNick(srcMsg:Message) extends Command(srcMsg) {
    def execute:ReplyBuilder = {
        val user = srcMsg.user
        val params = srcMsg.params
        if (params.list.length < 1) {
            ReplyBuilder(Reply.ERR_NONICKNAMEGIVEN)
        } else {
            (user.setNick(params.list.head)) match {
                case Success(newNick) => {
                    if (user.isRegistered) {
                        println("new nick: " + newNick)
                    }
                }
                case Failure(msg) => println("error: " + msg)
            }
            ReplyBuilder(Reply.RPL_NONE)
        }
    }
}