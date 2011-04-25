package commands

import util.parsing.combinator._
import db.User
import messages.parsers.{Tail, UserName, NickName, TargetsParser, ChannelName}
import messages.{ReplyBuilder, Reply, Message}

object DefaultCommands extends AbstractCommandSet {
    addCommand(UserCommand)
    addCommand(NickCommand)
    addCommand(MOTD)
}


object UserCommand extends AbstractParameterCommand[(UserName,Tail)]("user") {
    def paramParser:Parser[(UserName, Tail)] = username~nonWhiteSpace~nonWhiteSpace~tail ^^ {
        case user~nws1~nws2~realname => (user, realname)
    }

    def processParams(param: (UserName, Tail), u : User) = {
        val userRecord = u.record
        val nick = u.record.nick
        if (userRecord.registered) {
            new ReplyBuilder(nick, Reply.ERR_ALREADYREGISTRED)
        } else {
            u.username(param._1.name)
            u.realname(param._2.tail)
            new ReplyBuilder(nick, Reply.RPL_NONE)
        }
    }
}
object NickCommand extends AbstractParameterCommand[NickName]("nick") {
    //TODO: nick length check
    //TODO: notify all
    def processParams(nick: NickName, u: User) = {
        val oldNick = u.record.nick

        if (nick.inUse) {
            new ReplyBuilder(oldNick, Reply.ERR_NICKNAMEINUSE, nick.name)
        } else {
            u.nick(nick.name)
            new ReplyBuilder(oldNick, Reply.RPL_NONE)
        }
    }

    def paramParser = nickname
}
object MOTD extends AbstractCommandExecutable("motd") {
    def execute(msg: Message) = {
        val nick = msg.user.record.nick
        val reply = new ReplyBuilder(nick,Reply.RPL_MOTDSTART)
        reply.append(Reply.RPL_MOTD, "Test MOTD")
        reply.append(Reply.RPL_ENDOFMOTD)
    }
}

//object Join extends AbstractParameterCommand[(List[ChannelName],List[String])]("join") {
//    def paramParser = repsep(channel,",")~opt(nonwhitespace)/*~opt(repsep(nonWhiteSpace),",")*/
//    def apply(param: (List[ChannelName], Option[String]), u:User) {
//        val chans = param._1
//        val joinMessage = param._2.getOrElse { "" }
//        chans.foreach (c=> {
//            if (c.exists && user.canJoin(c) && !user.isIn(c)) {
//                u.join(c,joinMessage)
//            }
//        })
//        ReplyBuilder(Reply.RPL_NONE)
//    }
//}