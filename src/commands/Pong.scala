package commands

import messages._

class Pong(srcMsg : Message) extends AbstractCommand(srcMsg) {
    def execute:ReplyBuilder = {
        println("Pong message")
        ReplyBuilder(Reply.RPL_NONE)
    }
}