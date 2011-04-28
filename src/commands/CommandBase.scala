package commands

import messages._
import parsers.{CommandParser, Params}
import db.User
import collection.mutable.ArrayBuffer

//TODO: Create a CommandBuilder

//TODO: Should Commands extend (msg) => ReplyBuilder and have an apply method?
sealed class CommandBase //just to get IntelliJ to shut up

trait Executable {
    def execute(msg:UserMessage):ReplyBuilder
}

abstract class AbstractCommandExecutable(val name: String) extends Executable


//TODO: Investigate whether or not I actually need to separate
//parameters from the command.

abstract class AbstractParameterCommand[T](name: String)
    extends AbstractCommandExecutable(name) with CommandParser {

    def parseParams(params: String):T = {
        def printResult(resultType: String, msg: String, next: Input) {
            println("("+resultType+") msg/T: " + msg + ", next: " + next.source)
        }

        val result = parseAll(paramParser, params)
         result match {
            case Error(msg, next) => printResult("error",msg, next)
            case Failure(msg, next) => printResult("failure",msg, next)
            case Success(t, next) => {
                printResult("success", t.toString, next)
            }
        }
        result.get
    }

    def execute(msg:UserMessage):ReplyBuilder = {
        val parseOutput = parseParams(msg.params)
        processParams(parseOutput, msg.user, new ReplyBuilder(msg.user.record.nick))
    }

    protected def paramParser: Parser[T]

    def processParams(t: T, u: User, replyBuilder: ReplyBuilder):ReplyBuilder
}

