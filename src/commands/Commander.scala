package commands

import collection.mutable.HashMap
import messages.{Reply, Message}
import db.{UserModel, User}

object Commander {
    object UnknownCommand extends Executable {
        override def execute(msg:Message) = {
            ReplyBuilder(Reply.ERR_UNKNOWNCOMMAND, msg.command)
        }
    }

    val _commandMap = HashMap.empty[String, AbstractCommandExecutable]

    def registerCommands(acs: AbstractCommandSet) {
        acs.getCommands.foreach({(c) =>
            _commandMap += ((c.name.toLowerCase,c))
        })
    }

    def execute(msg: Message) = {
        val reply = getCommand(msg.command).execute(msg).get

        if (!msg.user.record.registered) {
            val command = msg.command.toUpperCase
            if (command == "NICK") {
                msg.user.hasSetNick = true
            }
            else if(command == "USER") {
                msg.user.hasSetUser = true
            }
            if (msg.user.hasSetNick && msg.user.hasSetNick) {
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