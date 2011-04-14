package commands

import messages._
import parsers.Params
import util.parsing.combinator._

abstract class AbstractCommand(srcMsg:Message) {
    def execute():ReplyBuilder
}

case class ReplyBuilder(reply:Reply, params: String*) {
    def getMessage():String = {
        val paramsArr = params.toArray
        reply.createMessage(paramsArr)
    }
}

trait ValidParams {
    def assertParamLength(params: Params, minSize: Int, requireTail: Boolean) = {
        (params.list.length < minSize) match {
            case false => false
            case true if requireTail => {
                if (params.tail.trim.isEmpty) false
                else true
            }
            case _ => true
        }
    }
}