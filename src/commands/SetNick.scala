package commands

import messages._

class SetNick(srcMsg:Message) extends AbstractCommand(srcMsg) {
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