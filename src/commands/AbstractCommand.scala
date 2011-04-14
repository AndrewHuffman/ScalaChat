package commands

import messages._
import parsers.Params
import util.parsing.combinator.Parsers

abstract class AbstractCommand {
    def execute(msg:Message):ReplyBuilder
}

//TODO: Use an implicit for the parser
abstract class AbstractRichCommand(name:String) extends AbstractCommand

case class ReplyBuilder(reply:Reply, params: String*) {
    def getMessage():String = {
        val paramsArr = params.toArray
        reply.createMessage(paramsArr)
    }
}