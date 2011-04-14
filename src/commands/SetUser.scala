package commands

import messages.{Reply, Message}

class SetUser(srcMsg:Message) extends AbstractCommand(srcMsg) {
    def execute:ReplyBuilder = {
        val user = srcMsg.user
        val params = srcMsg.params
        if (params.list.length < 3 && !params.tail.trim.isEmpty) {
            ReplyBuilder(Reply.ERR_NEEDMOREPARAMS, srcMsg.command)
        } else {
            ReplyBuilder(Reply.RPL_NONE)
        }
    }
}