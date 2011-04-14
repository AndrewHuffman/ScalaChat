package commands

import messages._

class Ping(srcMsg : Message) extends AbstractCommand(srcMsg) {
    def execute:ReplyBuilder = {
        println("Ping message")
        ReplyBuilder(Reply.RPL_NONE)
    }
}