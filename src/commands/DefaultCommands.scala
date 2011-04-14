package commands

import messages.{Reply, Message}
import messages.parsers.{NickName, TargetsParser}
import util.parsing.combinator._

object DefaultCommands

class UnknownCommand extends AbstractCommand {
    def execute(msg:Message) = {
        ReplyBuilder(Reply.ERR_UNKNOWNCOMMAND, msg.command)
    }
}

object TestParser extends TargetsParser {
    def parseMe(string: String) = parseAll(nickname,string).get
}

class Nick extends AbstractRichCommand("nick") {
    //(Parser[T] => T) => ReplyBuilder



    def execute(srcMsg: Message) = {
        val nick = TestParser.parseMe(srcMsg.params.params)
        nick.inUse match {
            case true => ReplyBuilder(Reply.ERR_NICKNAMEINUSE, nick.name)
            case false => ReplyBuilder(Reply.RPL_CUSTOM, "nickname changed to " + nick.name)
        }
    }
}