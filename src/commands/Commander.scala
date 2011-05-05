package commands

import collection.mutable.{HashSet, HashMap}
import actors.Actor._
import org.apache.log4j.Logger
import messages.{Replies, UserMessage, ReplyMessage, ReplyBuilder}
import targets.User

case class ReplyString(out: String)
case object Stop
case class MessageToBeExecuted(msg: UserMessage)

/**
 * Commander
 *
 * Handles all commands executable on this server. All comands
 * registered through registereCommands are avaiable for execution.
 * During executiong, any message recieved that does not exists here
 * is translated into an UnknownCommand and an error message is
 * sent to the user.
 */
object Commander {
    val logger = Logger.getLogger("Commander")
    val _commandMap = HashMap.empty[String, AbstractCommandExecutable]


    /**
     * The Executable that is executed in the event
     * the command supplied is unkown to the Commander.
     */
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

    /**
     * The Welcome message that creates the user once registered.
     */
    object Welcome extends Executable {
        def execute(msg: UserMessage) = {
            val reply = new ReplyBuilder(msg.user)
            reply.append(Replies.RPL_WELCOME)
            reply.append(Replies.RPL_HOST).append(Replies.RPL_CREATED)
        }
    }

    def messageReceiver = actor {
        var active = true
        while(active) {
            receive {
                case MessageToBeExecuted(msg) => {
                    execute(msg)
                }
                case Stop => active = false
            }
        }
    }

    /**
     * Registers the commands to the Commander
     * enabling the commands to be executed
     *
     */
    def registerCommands(acs: AbstractCommandSet) {
        acs.getCommands.foreach({(c) =>
            _commandMap += ((c.name.toLowerCase,c))
        })
    }

    /**
     * Takes the given UserMessage, executes the command
     * that matches the command provided by UserMessage
     * and returns a ReplyBuilder object that contains the
     * respons(es) to the user.
     *
     * If the command provided does not match a registered
     * command, an UnknownCommand message is generated
     */
    private def execute(msg: UserMessage) {
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

        user.send(reply.get);
    }

    /**
     * Retreives the Executable that matches the
     * given commandName
     */
    def getCommand(commandName: String):Executable = {
        _commandMap.get(commandName.toLowerCase) match {
            case Some(command) => command
            case None => UnknownCommand
        }
    }
}