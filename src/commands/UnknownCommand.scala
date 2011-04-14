package commands

import messages.{Reply, Message}

class UnknownCommand(srcMsg: Message) extends AbstractCommand(srcMsg) {
    def execute:ReplyBuilder = {
        ReplyBuilder(Reply.ERR_UNKNOWNCOMMAND, srcMsg.command);
    }
}