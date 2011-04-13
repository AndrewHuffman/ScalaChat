package commands

import messages.{Reply, ReplyBuilder, Message}

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/24/11
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */

class SetUser(srcMsg:Message) extends Command(srcMsg) {
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