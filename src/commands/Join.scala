package commands

import messages.{Reply, ReplyBuilder, Message}

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 4/5/11
 * Time: 8:30 PM
 * To change this template use File | Settings | File Templates.
 */

class Join(srcMsg: Message) extends Command(srcMsg) with ValidParams {
    def execute = {
//        val params = srcMsg.params
//        if (assertParamLength(params, 1, false)) {
//            val paramList = params.list
//
//        }
        ReplyBuilder(Reply.RPL_NONE)
    }
}