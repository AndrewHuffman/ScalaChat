package commands

import messages.{Reply, ReplyBuilder, Message}

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/24/11
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */

class Ping(srcMsg : Message) extends Command(srcMsg) {
    def execute:ReplyBuilder = {
        println("Ping message")
        ReplyBuilder(Reply.RPL_NONE)
    }
}