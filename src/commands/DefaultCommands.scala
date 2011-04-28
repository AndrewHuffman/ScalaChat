package commands

import util.parsing.combinator._
import db.User
import messages.parsers.{Tail, UserName, NickName, TargetsParser, ChannelName}
import messages.{Replies, UserMessage, ReplyBuilder}

object DefaultCommands extends AbstractCommandSet {
    addCommand(UserCommand)
    addCommand(NickCommand)
    addCommand(MOTD)
}


object UserCommand extends AbstractParameterCommand[(UserName,Tail)]("user") {
    def paramParser:Parser[(UserName, Tail)] = username~nonWhiteSpace~nonWhiteSpace~tail ^^ {
        case user~nws1~nws2~realname => (user, realname)
    }

    override def processParams(param: (UserName, Tail), u : User, reply: ReplyBuilder) = {
        val userRecord = u.record
        val nick = u.record.nick
        if (userRecord.registered) {
            reply.append(Replies.ERR_ALREADYREGISTRED)
        } else {
            u.username(param._1.name)
            u.realname(param._2.tail)
            reply.append(Replies.RPL_NONE)
        }
    }
}
object NickCommand extends AbstractParameterCommand[NickName]("nick") {
    //TODO: nick length check
    //TODO: notify all
    override def processParams(nick: NickName, u: User, reply: ReplyBuilder) = {
        val userRecord = u.record
        val oldNick = if (userRecord.registered) u.record.nick else ""

        if (nick.inUse) {
            reply.append(Replies.ERR_NICKNAMEINUSE(nick.name))
        } else {
            u.nick(nick.name)
            reply.append(Replies.RPL_NONE)
        }
    }

    def paramParser = opt(":")~>nickname
}

object MOTD extends AbstractCommandExecutable("motd") {
    def execute(msg: UserMessage) = {
        val nick = msg.user.record.nick
        val reply = new ReplyBuilder(nick)
        reply.append(Replies.RPL_MOTDSTART)
        reply.append(Replies.RPL_MOTD("Welcome to ScalaChat"))
        reply.append(Replies.RPL_MOTD("This is a test MOTD that"))
        reply.append(Replies.RPL_MOTD("is long"))
        reply.append(Replies.RPL_MOTD("blah blah blah blah blah blah"))
        reply.append(Replies.RPL_MOTD("blah blah blah blah blah blah"))
        reply.append(Replies.RPL_MOTD("blah blah blah blah blah blah"))
        reply.append(Replies.RPL_MOTD("blah blah blah blah blah blah"))
        reply.append(Replies.RPL_MOTD("blah blah blah blah blah blah"))
        reply.append(Replies.RPL_MOTD("blah blah blah blah blah blah"))
        reply.append(Replies.RPL_MOTD("blah blah blah blah blah blah"))
        reply.append(Replies.RPL_MOTD("Don't do anything stupid"))
        reply.append(Replies.RPL_MOTD("Have a nice day!"))
        reply.append(Replies.RPL_ENDOFMOTD)
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

