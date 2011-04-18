package commands

import messages.{Reply, Message}
import db.{UserModel, User}
import collection.mutable.{HashSet, HashMap}

object Commander {
    object UnknownCommand extends Executable {
        override def execute(msg:Message) = {
            ReplyBuilder(Reply.ERR_UNKNOWNCOMMAND, msg.command)
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
        val reply = getCommand(msg.command).execute(msg).get

        /* TODO: Commands which require registration and initiate registration
        should be handled in a CommandSet */

        val isRegistered = msg.user.record.registered

        if (!isRegistered) {
            val command = msg.command.toUpperCase
            if (command == "NICK") {
                msg.user.hasSetNick = true
            }
            else if(command == "USER") {
                msg.user.hasSetUser = true
            }
            if (msg.user.hasSetNick && msg.user.hasSetUser) {
                UserModel.register(msg.user.id)
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