package commands

import util.parsing.combinator._
import targets.{Channels, User, Channel}
import messages.{Replies, UserMessage, ReplyBuilder, Messages}
import messages.parsers._

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


/***
 * User command
 *
 * example: User <user> <host> <servername> :<realname>
 *
 * Used in the registration process when the user connects.
 * <host> and <servername> are ignored since this is not
 * typically applicable.
 */
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

/***
 * Nick command
 *
 * example: Nick <nickname>
 *
 * Changes the user's nick, if possible, to the given nickname
 */
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

/***
 * Join command
 *
 * example: JOIN <channel> {, <channel>}
 *
 * Joins the provided channels, if possible. Creates a Channel
 * if the channel doesn't already exist.
 *
 * TODO: Add support for keys
 */

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

/***
 * Part command
 *
 * example: PART <channel> {, <channel> }
 *
 * Will have the user who issued this command leave the given chanenls.
 */
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
/***
 * PrivMSg command
 *
 * example: PRIVMSG <channel/user> :<message>
 *
 * Sends the message to the given channel or user.
 */
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
/***
 * Names command
 *
 * example: NAMES <channel> {, <channel>}
 *
 * Displays the names of the users within the provided channels(s)
 *
 * TODO: Handle operators and voice-enabled
 */
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

/***
 * quit command
 *
 * example: QUIT [:QUIT MESSAGE]
 *
 * Disconnects the user from the user and, if a message is given
 * this will be displayed when the user disconnects.
 */
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
 * topic command
 *
 * example: TOPIC <channel> [new topic]
 *
 * If given [new topic], changes the topic of <channel>
 * if the user has permission.
 *
 * When [new topic] is omitted, it will display the topic of the
 * channel, if the user has permission.
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
/***
 * Ping command
 *
 * example: PING [:<ping message>]
 *
 * Server will respond to the client with a PONG
 */
object Ping extends AbstractParameterCommand[Option[Tail]]("ping") {
    override def processParams(param: Option[Tail], u: User, reply: ReplyBuilder, msg: UserMessage) = {
        val tailStr = param.getOrElse({Tail("")}).tail
        reply.append(Messages.PongMessage(tailStr))
        reply
    }
    def paramParser = opt(tail)
}

/***
 * List command
 *
 * example: List
 *
 * Lists all the channels
 *
 * TODO: Needs to Support parameters
 * TODO: Filter out p & s mode chans
 */
object List extends AbstractCommandExecutable("list") {
    def execute(msg: UserMessage) = {
        val reply = new ReplyBuilder(msg.user)
        reply.append(Replies.RPL_LISTSTART)
        Channels.getAll.foreach((channelRecord) => {
            val name = channelRecord.name
            val users = (new Channel(channelRecord)).numUsers
            val topic = channelRecord.topic
            reply.append(Replies.RPL_LIST(channelRecord.name, users.toString, topic))
        })
        reply.append(Replies.RPL_LISTEND)
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

trait UnsupportedCommand extends Executable {
    def execute(msg: UserMessage) = {
        val reply = new ReplyBuilder(msg.user)
        reply.append(Replies.RPL_COMMANDNOTSUPPORTED(msg.command))
    }
}

object Mode extends AbstractCommandExecutable("mode") with UnsupportedCommand
object UserHost extends AbstractCommandExecutable("userhost") with UnsupportedCommand
object Oper extends AbstractCommandExecutable("oper") with UnsupportedCommand
object Kick extends AbstractCommandExecutable("Kick") with UnsupportedCommand

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
    addCommand(Ping)
    addCommand(Mode)
    addCommand(UserHost)
    addCommand(Oper)
    addCommand(Kick)
    addCommand(List)
}