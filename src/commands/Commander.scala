package commands

import messages.{Reply, Message, ReplyBuilder}
import db.{UserModel, User}
import collection.mutable.{HashSet, HashMap}
import actors.Actor
import org.apache.log4j.Logger

object Commander {
    val logger = Logger.getLogger("Commander")

    object UnknownCommand extends Executable {
        override def execute(msg:Message) = {
            val usr = msg.user
            val nick = msg.user.record.nick
            if (isRegistered(usr))
                new ReplyBuilder(nick, Reply.ERR_UNKNOWNCOMMAND, msg.command)
            else
                new ReplyBuilder(nick, Reply.RPL_NONE)
        }
    }

    val _commandMap = HashMap.empty[String, AbstractCommandExecutable]
    val _isRegisteredCache = HashMap.empty[User, Boolean]

    def registerCommands(acs: AbstractCommandSet) {
        acs.getCommands.foreach({(c) =>
            _commandMap += ((c.name.toLowerCase,c))
        })
    }

    private def isRegistered(user: User) = {
        _isRegisteredCache.get(user) match {
            case Some(true) => true
            case Some(false) => {
                val isRegistered = user.record.registered
                _isRegisteredCache += ((user, isRegistered))
                isRegistered
            }
            //if user isn't in cache, then user couldn't have
            //registered as they wouldn't have executed any commands
            case None => {
                _isRegisteredCache += ((user, false))
                false
            }
        }
    }

    def execute(msg: Message) = {
        val reply = getCommand(msg.command).execute(msg)

        /* TODO: Commands which require registration and initiate registration
        should be handled differently*/
        val user = msg.user
        val isRegistered = user.record.registered

        if (!isRegistered) {
            val command = msg.command.toUpperCase
            if (command == "NICK") {
                user.hasSetNick = true
            }
            else if(command == "USER") {
                user.hasSetUser = true
            }
            if (user.hasSetNick && user.hasSetUser) {
                //TODO: This should not be here.
                UserModel.register(msg.user.id)
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