package commands

import util.parsing.combinator._
import messages.parsers.{Tail, UserName, NickName, TargetsParser, ChannelName}
import targets.{Channels, User}
import messages.{Replies, UserMessage, ReplyBuilder, Messages}

object DefaultCommands extends AbstractCommandSet {
    addCommand(UserCommand)
    addCommand(NickCommand)
    addCommand(MOTD)
    addCommand(Join)
    addCommand(Part)
    addCommand(Names)
    addCommand(Topic)
}


object UserCommand extends AbstractParameterCommand[(UserName,Tail)]("user") {
    def paramParser:Parser[(UserName, Tail)] = username~nonWhiteSpace~nonWhiteSpace~tail ^^ {
        case user~nws1~nws2~realname => (user, realname)
    }

    override def processParams(param: (UserName, Tail), u : User, reply: ReplyBuilder, msg: UserMessage) = {
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
    //TODO: notify all - needs to be unique for each user??
    override def processParams(nick: NickName, u: User, reply: ReplyBuilder, msg: UserMessage) = {
        val userRecord = u.record
        val oldNick = if (userRecord.registered) u.record.nick else ""

        if (nick.inUse) {
            reply.append(Replies.ERR_NICKNAMEINUSE(nick.name))
        } else {
            u.changeNick(nick.name)
            reply.append(Replies.RPL_NONE)
        }
    }

    def paramParser = opt(":")~>nickname
}


//TODO: Add support for keys
object Join extends AbstractParameterCommand[List[ChannelName]]("join") {
    override def processParams(params: List[ChannelName], u: User, reply: ReplyBuilder, msg: UserMessage) = {
        params.foreach(chan => {
            val chanName = chan.name
            val channel =
                chan.getChannel match {
                    case Some(chan) => chan
                    case None => Channels.create(chanName)
                }
            if (!u.isIn(channel)) {
                u.join(channel)
                channel.send(Messages.JoinMessage(chanName, u))
                if (!channel.getTopic.isEmpty) {
                    reply.append(Replies.RPL_TOPIC(chanName, channel.getTopic))
                }
                reply.append(Names.execute(msg))
            }
        })
        reply
    }

    def paramParser = repsep(channel,",")
}

object Part extends AbstractParameterCommand[List[ChannelName]]("part") {
    override def processParams(chans: List[ChannelName], u: User, reply: ReplyBuilder, msg: UserMessage) = {
        chans.foreach(chan => {
            chan.getChannel match {
                case Some(c) => {
                    c.send(Messages.PartMessage(c.record.name, u))
                    u.part(c)
                }
                case None => reply.append(Replies.ERR_NOSUCHCHANNEL(chan.name))
            }
        })
        reply
    }

    def paramParser = repsep(channel, ",")
}


object Names extends AbstractParameterCommand[List[ChannelName]]("names") {
    override def processParams(chans: List[ChannelName], u: User, reply: ReplyBuilder, msg: UserMessage) = {
        chans.foreach(chan => {
            val usrs =
                chan.getChannel match {
                    case Some(c) => {
                        for(c <- c.users) yield c.nick
                    }
                    case None => Nil
                }
            reply.append(Replies.RPL_NAMEREPLY(chan.name, usrs))
            reply.append(Replies.RPL_ENDOFNAMES(chan.name))
        })
        reply
    }

    def paramParser = repsep(channel, ",")
}

object Topic extends AbstractParameterCommand[(ChannelName,Option[Tail])]("topic") {
    override def processParams(param: (ChannelName, Option[Tail]), u: User, reply: ReplyBuilder, msg: UserMessage) = {
        val chanName = param._1
        val topic = param._2
        //TODO: Allow changing of topic
        chanName.getChannel match {
            case Some(channel) => {
                if (channel.containsUser(u)) {
                    if (channel.getTopic.isEmpty) {
                        reply.append(Replies.RPL_NOTOPIC(chanName.name))
                    } else {
                        reply.append(Replies.RPL_TOPIC(chanName.name, channel.getTopic))
                    }
                } else {
                    reply.append(Replies.ERR_NOTONCHANNEL(chanName.name))
                }
            }
            case None => reply.append(Replies.ERR_NOTONCHANNEL(chanName.name))
        }
    }

    def paramParser = channel~opt(tail) ^^ {
        case chan~topic => (chan, topic)
    }
}


object MOTD extends AbstractCommandExecutable("motd") {
    def execute(msg: UserMessage) = {
        val reply = new ReplyBuilder(msg.user)
        //TODO: This should be read in from a properties file
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
//
//class Mode(mode: Char) extends AbstractParameterCommand[("mode") {
//    def execute(msg: UserMessage) = {
//
//    }
//}