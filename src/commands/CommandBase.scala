package commands

import messages._
import parsers.{CommandParser, Params}
import util.parsing.combinator.Parsers
import db.User

//TODO: Create a CommandBuilder

//TODO: Should Commands extend (msg) => ReplyBuilder and have an apply method?
sealed class CommandBase //just to get IntelliJ to shut up

trait Executable {
    def execute(msg:Message):ReplyBuilder
}

abstract class AbstractCommandExecutable(val name: String) extends Executable

abstract class AbstractParameterCommand[+T](name: String, params: AbstractParameters[T])
    extends AbstractCommandExecutable(name) with CommandParser with Executable {

    implicit def paramsToString(p : Params) = {
        p.params
    }

    def paramParser: Parser[T]

    def execute(msg:Message):ReplyBuilder = {
        val parsedText = parseAll(paramParser, msg.params).get
        params(parsedText, msg.user)
    }
}

abstract class AbstractParameters[T] {
    def apply(t: T, u: User):ReplyBuilder
}

case class ReplyBuilder(reply:Reply, params: String*) {
    def get:String = {
        val paramsArr = params.toArray
        reply.createMessage(paramsArr)
    }
}