package commands

import ValidCommands._
import messages.Message

object CommandFactory {

    def getCommandFor(message: Message):AbstractCommand = {
        getCommandFor(message.command, message)
    }

    def getCommandFor(commandString:String, srcMsg: Message):AbstractCommand = {
        val commandEnum = ValidCommands.values.find(_.toString.equals(commandString.toUpperCase())) match {
            case Some(found) => found
            case None => UNKNOWN
        }
        getCommandFor(commandEnum, srcMsg)
    }

    def getCommandFor(commandEnum: ValidCommands, srcMsg: Message): AbstractCommand = {
        commandEnum match {
            case PING => new Ping(srcMsg)
            case PONG => new Pong(srcMsg)
            case NICK => new SetNick(srcMsg)
            case USER => new SetUser(srcMsg)
            case _ => new UnknownCommand(srcMsg)

        }
    }
}