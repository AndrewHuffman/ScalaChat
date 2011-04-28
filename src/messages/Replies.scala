package messages

import parsers.Params

object Reply {
    def msgBuilder(param: List[String], tail: String) = {
        val sb = new StringBuilder
        param.foreach(p => {
            sb.append(p).append(" ")
        })

        sb.append(":").append(tail).toString()
    }
}

case class Reply(code: Int, msg: String) {
    def this(code: Int, params: List[String], tail: String) = this(code, Reply.msgBuilder(params, tail))
    def this(code: Int, param: String, tail: String) = this(code,List(param),tail)

    def getCode = "%03d".format(code)

    def getFormattedMessage = msg
}

object Replies {
    case object RPL_WELCOME extends Reply(1, ":Welcome to the ScalaChat Test IRC Server")
    case object RPL_HOST extends Reply(2, ":Your host is localhost[127.0.0.1/6667]")
    case object RPL_CREATED extends Reply(3, ":This server was created today.")
    //REAL
	case object RPL_NONE extends Reply(300,"")
    case object RPL_MOTDSTART extends Reply(375, ":- 127.0.0.1 Message of the day  -")
    case class RPL_MOTD(motd: String) extends Reply(372, ":"+ motd)
    case object RPL_ENDOFMOTD extends Reply(376, ":End of /MOTD command")
    case class RPL_CUSTOM(customMsg: String) extends Reply(200, ":"+ customMsg)
	//301-400
	/* Error Replies */
	case class ERR_NOSUCHNICK(nick: String) extends Reply(401, nick, "No such nick/channel")
	case class ERR_NOSUCHSERVER(server: String) extends Reply(402, server, "No such server")
	case class ERR_NOSUCHCHANNEL(chan: String) extends Reply(403, chan, "No such channel")
	case class ERR_CANNOTSENDTOCHAN(chan: String) extends Reply(404, chan, "Cannot send to channel")
	case class ERR_TOOMANYCHANNELS(chan: String) extends Reply(405, chan, "You have joined too many channels")
	case class ERR_WASNOSUCHNICK(nick: String) extends Reply(406, nick, "There was no such nickname")
	case object ERR_TOOMANYTARGETS extends Reply(407, ":Duplicate recipients. No message delivered")
	//408-410
	case class ERR_NORECIPIENT(command: String) extends Reply(411, " :No recipient given ("+command+")")
	case object ERR_NOTEXTTOSEND extends Reply(412, ":No text to send")
	case class ERR_UNKNOWNCOMMAND(command: String) extends Reply(421, command, "Unknown command")
	//422-431
//    case object ERR_NONICKNAMEGIVEN extends Reply(431, "No nickname given")
	case class ERR_ERRONEUSNICKNAME(nick: String) extends Reply(432, nick, "Erroneus nickname")
	case class ERR_NICKNAMEINUSE(nick: String) extends Reply(433, nick, "Nickname is already in use")
//	case object ERR_USERNOTINCHANNEL extends Reply(441, "They aren't on that channel")
//	case object ERR_NOTONCHANNEL extends Reply(442, "You're not on that channel")
//	case object ERR_USERONCHANNEL extends Reply(443, "is already on channel")
//	//444-450
//	case object ERR_NOTREGISTERED extends Reply(451, "You have not registered")
	case class ERR_NEEDMOREPARAMS(command: String) extends Reply(461, command, "Not enough parameters")
    case object ERR_ALREADYREGISTRED extends Reply(453, "You may not reregister")
//	//454-466
//	case object ERR_KEYSET extends Reply(467, "Channel key already set")
//	//468-470 NONE
//	case object ERR_CHANNELISFULL extends Reply(471, "Cannot join channel (+l)")
//	case object ERR_UNKNOWNMODE extends Reply(472, "is unknown mode char to me")
//	case object ERR_INVITEONLYCHAN extends Reply(473, "Cannot join channel (+i)")
//	case object ERR_BANNEDFROMCHAN extends Reply(474, "Cannot join channel (+b)")
//	case object ERR_BADCHANNELKEY extends Reply(475, "Cannot join channel (+k)")
//	//476-481 NONE
//	case object ERR_NOPRIVILEGES extends Reply(481, "Permission Denied- You're not an IRC operator")
//	case object ERR_CHANOPRIVSNEEDED extends Reply(482, "You're not channel operator")
	//483-502
}

//TODO: Externalize server name
class ReplyMessage(val nick: String, val reply: Reply = Replies.RPL_NONE)
    extends Message("my.server.com", reply.getCode, Params(nick + " " + reply.getFormattedMessage))