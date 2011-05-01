package commands

import util.parsing.combinator._
import targets.{Channels, User, Channel}
import messages.{Replies, UserMessage, ReplyBuilder, Messages}
import messages.parsers._

object DefaultCommands extends AbstractCommandSet {
    addCommand(UserCommand)
    addCommand(NickCommand)
    addCommand(MOTD)
    addCommand(Join)
    addCommand(Part)
    addCommand(Names)
    addCommand(Topic)
    addCommand(PrivMsg)
    addCommand(Quit)
}

/************************************************
 *                 COMMANDS
 *
 * The following objects are commands that a client
 * can send to the server. See CommandBase.scala
 * for indepth information on Commands.
 *
 * The following command comments will provide
 * an example or several examples. Required parameters
 * will be between <>'s, optional paramters
 * will be within []'s, and repetition will be
 * indicated through a *
 ************************************************/

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
    override def processParams(nick: NickName, u: User, reply: ReplyBuilder, msg: UserMessage) = {
        if (nick.exists) {
            reply.append(Replies.ERR_NICKNAMEINUSE(nick.name))
        } else {
            val newNickname = nick.name.substring(0, if (nick.name.length > 15) 15 else nick.name.length)
            u.broadcast(Messages.NickMessage(newNickname, u))
            u.changeNick(newNickname)
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

object PrivMsg extends AbstractParameterCommand[(Either[ChannelName, NickName],Tail)]("privmsg") {
    override def processParams(params: (Either[ChannelName, NickName],Tail), u: User, reply: ReplyBuilder, msg: UserMessage) = {
        val target = params._1
        val msg = params._2
        target match {
            case Left(chan) => {
                if (chan.exists) {
                    val channel = chan.getChannel.get
                     if (u.isIn(channel) || !channel.record.n_mode) {
                         channel.sendOmit(Messages.PrivateMessage(chan.name, u, msg.tail), u)
                     } else {
                         reply.append(Replies.ERR_CANNOTSENDTOCHAN(chan.name))
                     }
                } else {
                    reply.append(Replies.ERR_NOSUCHCHANNEL(chan.name))
                }
            }
            case Right(nick) => {
                nick.getUser match {
                    case Some(user) => user.send(Messages.PrivateMessage(nick.name, u, msg.tail))
                    case None => reply.append(Replies.ERR_NOSUCHNICK(nick.name))
                }
            }
        }
        reply.append(Replies.RPL_NONE)
    }

    def paramParser = {
        channel~tail ^^ {
            case channel~tail => (Left(channel),tail)
        } |
            nickname~tail ^^ {
                case nick~tail => (Right(nick),tail)
            }
    }
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

object Quit extends AbstractParameterCommand[Option[Tail]]("quit") {
    override def processParams(param: Option[Tail], u: User, reply: ReplyBuilder, msg: UserMessage) = {
        val quitMsg = param match {
            case Some(Tail(msg)) => msg
            case None => ""
        }
        u.broadcast(Messages.QuitMessage(quitMsg, u))
        u.quit()
        reply
    }

    def paramParser = opt(tail)
}

/***
 * Topic command
 *
 * example: TOPIC <channel> [new topic]
 */
object Topic extends AbstractParameterCommand[(ChannelName,Option[Tail])]("topic") {
    override def processParams(param: (ChannelName, Option[Tail]), u: User, reply: ReplyBuilder, msg: UserMessage) = {
        val chanName = param._1
        val tail = param._2

        chanName.getChannel match {
            /* Channel exists */
            case Some(channel) => u.isIn(channel) match {
                /* User is within channel */
                case true => tail match {
                    /* User provided a topic, so the topic will be chaned to the given topic*/
                    case Some(Tail(topic)) => {
                        (u.isOpIn(channel) || !channel.record.t_mode) match {
                            case true => {
                                channel.setTopic(topic)
                                channel.send(Messages.TopicMessage(topic, chanName.name, u))
                            }
                            case false => reply.append(Replies.ERR_CHANOPRIVSNEEDED(chanName.name))
                        }
                    }
                    /* User didn't provide a topic, so display the topic (or a No topic message) */
                    case None => channel.getTopic match {
                        case "" => reply.append(Replies.RPL_NOTOPIC(chanName.name))
                        case _ => reply.append(Replies.RPL_TOPIC(chanName.name, channel.getTopic))
                    }

                }
                /* User isn't within the channel */
                case false => reply.append(Replies.ERR_NOTONCHANNEL(chanName.name))
            }
            /* Channel Doesn't exist */
            case None => reply.append(Replies.ERR_NOSUCHCHANNEL(chanName.name))
        }
        reply
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

//object Ping extends AbstractParameterCommand[Option[Tail]]


//
//class Mode(mode: Char) extends AbstractParameterCommand[("mode") {
//    def execute(msg: UserMessage) = {
//
//    }
//}