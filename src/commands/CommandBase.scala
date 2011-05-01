package commands

import messages._
import parsers.{ParameterParser, Params}
import collection.mutable.ArrayBuffer
import targets.User

//TODO: Create a CommandBuilder

//TODO: Should Commands extend (msg) => ReplyBuilder and have an apply method?
sealed class CommandBase //just to get IntelliJ to shut up

trait Executable {
    def execute(msg:UserMessage):ReplyBuilder
}

abstract class AbstractCommandExecutable(val name: String) extends Executable

abstract class AbstractParameterCommand[T](name: String)
    extends AbstractCommandExecutable(name) with ParameterParser {

    def parseParams(params: String):Option[T] = {
        def printResult(resultType: String, msg: String, next: Input) {
            println("("+resultType+") msg/T: " + msg + ", next: " + next.source)
        }

        val result = parseAll(paramParser, params)
         val ret = result match {
            case Error(msg, next) => {
                printResult("error",msg, next)
                None
            }
            case Failure(msg, next) => {
                printResult("failure",msg, next)
                None
            }
            case Success(t, next) => {
                printResult("success", t.toString, next)
                Some(result.get)
            }
        }
        ret
    }

    def execute(msg:UserMessage):ReplyBuilder = {
        val parseOutput = parseParams(msg.params)
        parseOutput match {
            case Some(out) => {
                processParams(out, msg.user, new ReplyBuilder(msg.user), msg)
            }
            case None => {
                (new ReplyBuilder(msg.user)).append(Replies.ERR_NEEDMOREPARAMS(msg.command))
            }
        }

    }

    protected def paramParser: Parser[T]

    //TODO: Change signature to be (T, UserMEssage, ReplyBuildeR)
    def processParams(t: T, u: User, replyBuilder: ReplyBuilder, msg: UserMessage):ReplyBuilder
}

