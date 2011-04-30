package commands

import collection.mutable.{HashSet, HashMap}
import actors.Actor
import org.apache.log4j.Logger
import messages.{Replies, UserMessage, ReplyMessage, ReplyBuilder}
import targets.User

object Commander {
    val logger = Logger.getLogger("Commander")
    val _commandMap = HashMap.empty[String, AbstractCommandExecutable]

    object UnknownCommand extends Executable {
        override def execute(msg:UserMessage) = {
            val usr = msg.user
            val reply = new ReplyBuilder(msg.user)
            if (usr.record.registered)
                reply.append(Replies.ERR_UNKNOWNCOMMAND(msg.command))
            else
                reply.append(Replies.RPL_NONE)
        }
    }

    object Welcome extends Executable {
        def execute(msg: UserMessage) = {
            val reply = new ReplyBuilder(msg.user)
            reply.append(Replies.RPL_WELCOME)
            reply.append(Replies.RPL_HOST).append(Replies.RPL_CREATED)
        }
    }

    def registerCommands(acs: AbstractCommandSet) {
        acs.getCommands.foreach({(c) =>
            _commandMap += ((c.name.toLowerCase,c))
        })
    }

    def execute(msg: UserMessage) = {
        val reply = getCommand(msg.command).execute(msg)

        /* Not sure this is the place for registration. */
        val user = msg.user
        val userRecord = user.record

        if (!userRecord.registered) {
            if (userRecord.nick.nonEmpty && userRecord.user.nonEmpty) {
                user.register()
                reply.append(Welcome.execute(msg))
                reply.append(getCommand("motd").execute(msg))
            }
        }

        reply
    }

    def getCommand(commandName: String):Executable = {
        _commandMap.get(commandName.toLowerCase) match {
            case Some(command) => command
            case None => UnknownCommand
        }
    }
}