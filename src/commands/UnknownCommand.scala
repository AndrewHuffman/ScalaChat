package commands

import messages.{Reply, Message, ReplyBuilder}

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/24/11
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */

class UnknownCommand(srcMsg: Message) extends Command(srcMsg) {
    def execute:ReplyBuilder = {
        ReplyBuilder(Reply.ERR_UNKNOWNCOMMAND, srcMsg.command);
    }
}