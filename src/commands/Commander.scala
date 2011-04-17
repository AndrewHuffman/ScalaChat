package commands

import collection.mutable.HashMap
import messages.{Reply, Message}
import targets.User

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
        _commandMap.get(msg.command.toLowerCase) match {
            case Some(command) => command.execute(msg)
            case None => UnknownCommand.execute(msg)
        }
    }
}