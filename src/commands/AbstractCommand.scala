package commands

import messages._
import parsers.Params
import util.parsing.combinator.Parsers


//TODO: Should Commands extend (msg) => ReplyBuilder and have an apply method?
abstract class AbstractCommand(val name: String) {
    def execute(msg:Message):ReplyBuilder
}

case class ReplyBuilder(reply:Reply, params: String*) {
    def get:String = {
        val paramsArr = params.toArray
        reply.createMessage(paramsArr)
    }
}