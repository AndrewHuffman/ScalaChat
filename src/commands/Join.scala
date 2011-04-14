package commands

import messages.{Reply, Message}

class Join(srcMsg: Message) extends AbstractCommand(srcMsg) with ValidParams {
    def execute = {
        val params = srcMsg.params
        if (assertParamLength(params, 1, false)) {
            val paramList = params.list

        }
        ReplyBuilder(Reply.RPL_NONE)
    }
}