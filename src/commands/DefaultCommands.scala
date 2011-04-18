package commands

import messages.{Reply, Message}
import util.parsing.combinator._
import db.User
import messages.parsers.{Tail, UserName, NickName, TargetsParser}

object DefaultCommands extends AbstractCommandSet {
    addCommand(NickCommand)
}

object TestParser extends TargetsParser {
    def parseMe(in: String) = parseAll(nickname,in).get
}

object NickParams extends AbstractParameters[(NickName)] {
    def apply(nick: (NickName), u: User) = {
        if (nick.inUse) {
            ReplyBuilder(Reply.ERR_NICKNAMEINUSE)
        } else {
            u.nick(nick.name)
            ReplyBuilder(Reply.RPL_NONE)
        }
    }
}

object NickCommand extends AbstractParameterCommand[(NickName)]("nick", NickParams) {
    def paramParser = nickname
}

object UserCommand extends AbstractParameterCommand[(UserName,Tail)]("user", UserParams) {
    def paramParser:Parser[(UserName, Tail)] = username~tail ^^ {
        case username~tail => (username, tail)
    }
}

object UserParams extends AbstractParameters[(UserName, Tail)] {
    def apply(param: (UserName, Tail), u : User) = {
        ReplyBuilder(Reply.RPL_NONE)
    }
}
//object Nick extends AbstractCommand("nick") {
//    //(Parser[T] => T) => ReplyBuilder
//    def execute(srcMsg: Message) = {
//        val nick = TestParser.parseMe(srcMsg.params.params)
//        nick.inUse match {
//            case true => ReplyBuilder(Reply.ERR_NICKNAMEINUSE, nick.name)
//            case false => ReplyBuilder(Reply.RPL_CUSTOM, "nickname changed to " + nick.name)
//        }
//    }
//}