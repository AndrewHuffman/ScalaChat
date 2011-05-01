package net

import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.util.UUID
import collection.mutable.ArrayBuffer
import commands.{Commander}
import db.tables.UserTable
import db.{IRCDB}
import targets.User
import util.parsing.combinator.Parsers
import messages.parsers.ParseSuccess._
import messages.{Replies, ReplyBuilder, Message}
import messages.parsers.{ParseFailure, ParseSuccess, MessageParser}

//TODO: Server will have actor which handles messages from the clients
class UserConnection(socket: Socket) extends Thread {
    //val host = socket.getInetAddress.getCanonicalHostName
    val uuid = UUID.randomUUID().toString()
    val user:User = new User(this, new UserTable(host = socket.getInetAddress.getCanonicalHostName, userConnID = uuid.toString))

    private val out = new PrintStream(socket.getOutputStream)
    private val in  = new BufferedReader(new InputStreamReader(socket.getInputStream))
    private val parser = new MessageParser(user)

    override def run() {
        println("my.server.com <<-->>" + user.record.host)
        def timeSince(time: Long) = System.currentTimeMillis - time
        var line = in.readLine

        while(line != null) {
            println("<-" + line)

            val parserResult = parser.parseLine(line.trim)
            val reply = parserResult match {
                case ParseSuccess(msgToken) => {
                    //val command = msgToken.command.toUpperCase
                    //val time = System.currentTimeMillis
                    Commander.execute(msgToken)
//                    val timeDelta = timeSince(time)
//                    println("dT ("+command+"): " + timeDelta)
                }
                case ParseFailure() => {
                    (new ReplyBuilder(user)).append(Replies.RPL_CUSTOM("Error processing commands"))
                }
            }

            user.send(reply.get)

            if (!socket.isClosed) line = in.readLine
            else line = null
        }
        println("DELETING USER...." + user.record.nick)
        user.delete()
    }

    def send(msg: String) {
        //don't send empty messages.
        if (!msg.trim.isEmpty) {
            out.println(msg)
            println("-> " + msg)
        }
    }

    def disconnect() {
        socket.close()
    }
}