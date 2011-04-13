package commands

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/24/11
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */

import ValidCommands._
import messages.Message

object CommandFactory {

    def getCommandFor(message: Message):Command = {
        getCommandFor(message.command, message)
    }

    def getCommandFor(commandString:String, srcMsg: Message):Command = {
        val commandEnum = ValidCommands.values.find(_.toString.equals(commandString.toUpperCase())) match {
            case Some(found) => found
            case None => UNKNOWN
        }
        getCommandFor(commandEnum, srcMsg)
    }

    def getCommandFor(commandEnum: ValidCommands, srcMsg: Message): Command = {
        commandEnum match {
            case PING => new Ping(srcMsg)
            case PONG => new Pong(srcMsg)
            case NICK => new SetNick(srcMsg)
            case USER => new SetUser(srcMsg)
            case _ => new UnknownCommand(srcMsg)

        }
    }
}