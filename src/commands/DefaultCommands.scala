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
    //TODO: nick length check
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
    def paramParser:Parser[(UserName, Tail)] = username~nonWhiteSpace~nonWhiteSpace~tail ^^ {
        case user~nws1~nws2~realname => (user, realname)
    }
}

object UserParams extends AbstractParameters[(UserName, Tail)] {
    def apply(param: (UserName, Tail), u : User) = {
        val userRecord = u.record
        if (userRecord.registered) {
            ReplyBuilder(Reply.ERR_ALREADYREGISTRED)
        } else {
            ReplyBuilder(Reply.RPL_NONE)
        }
    }
}