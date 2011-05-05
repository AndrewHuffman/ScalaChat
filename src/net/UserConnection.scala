package net

import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.util.UUID
import collection.mutable.ArrayBuffer
import db.tables.UserTable
import db.{IRCDB}
import targets.User
import util.parsing.combinator.Parsers
import messages.parsers.ParseSuccess._
import messages.{Replies, ReplyBuilder, Message}
import messages.parsers.{ParseFailure, ParseSuccess, MessageParser}
import commands.{MessageToBeExecuted, Commander}

/**
 * Class that represents a connection to the server.
 * This class extends Thread, thus each connection will
 * run on a separate thread
 */
class UserConnection(socket: Socket) extends Thread {
    /**
     * UUID of this connection
     */
    val uuid = UUID.randomUUID().toString()
    /**
     * User object of the user that just connected
     */
    val user:User = new User(this, new UserTable(host = socket.getInetAddress.getCanonicalHostName, userConnID = uuid.toString))

    /**
     * Output stream to connection
     */
    private val out = new PrintStream(socket.getOutputStream)
    /**
     * Input stream from connection
     */
    private val in  = new BufferedReader(new InputStreamReader(socket.getInputStream))
    /**
     * Parser for this connection. Parsers aren't thread-safe, and thus
     * must be recreated for each connection.
     */
    private val parser = new MessageParser(user)

    /**
     * Starts a thread that handles all User inputs.
     * Each input recieved is parsed, converted into
     * a Command object that is then executed. Any
     * command replies generated are sent to the user.
     */
    override def run() {
        println("my.server.com <<-->>" + user.record.host)
        var line = in.readLine

        /* Read in each line */
        while(line != null) {
            println("<-" + line)
            /* Parse the line into a structured UserMessage object */
            val parserResult = parser.parseLine(line.trim)

            /**
             * Determine if parsing was successful. If unsuccessful, display
             * to user an error message. This math returns a replyBuilder
             * that will be sent to the user.
             */
            parserResult match {
                case ParseSuccess(userMsg) => {
                    Commander.messageReceiver ! MessageToBeExecuted(userMsg)
                }
                case ParseFailure() => {
                    val reply = (new ReplyBuilder(user)).append(Replies.RPL_CUSTOM("Error processing commands"))
                    user.send(reply.get);
                }
            }

            if (!socket.isClosed) line = in.readLine
            else line = null
        }

        /**
         * Deletes the user from the database.
         */
        println("DELETING USER...." + user.record.nick)
        user.delete()
    }

    /**
     * Sends the string to the user.
     * If the string is empty nothing is sent
     */
    def send(msg: String) {
        //don't send empty messages.
        if (!msg.trim.isEmpty) {
            out.println(msg)
            println("-> " + msg)
        }
    }

    /**
     * Close the connection.
     */
    def disconnect() {
        socket.close()
    }
}