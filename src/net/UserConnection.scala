package net

import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import messages.parsers.MessageParser
import java.util.UUID
import collection.mutable.ArrayBuffer
import commands.{Commander}
import db.{UserModel, UserTable, IRCDB, User}
import messages.{ReplyBuilder, Message}

//TODO: Server will have actor which handles messages from the clients
class UserConnection(socket: Socket) extends Thread {

    private val out = new PrintStream(socket.getOutputStream)
    private val in  = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val host = socket.getInetAddress.getCanonicalHostName

    /**
     * TODO:Fix
     *
     * The row for the user must be created when the connection is made.
     * The nick field is unique and non-null, so it must be populated.
     * I initialize the nick to a random UUID in order to add the row.
     *
     */
    private val row = UserModel.insert(new UserTable(0, UUID.randomUUID.toString))
    private val user = new User(this, row.id)

    private val parser = new MessageParser(user)

    override def run() {
        println(" <<-->>" + host)
        user.host(host)
        def timeSince(time: Long) = System.currentTimeMillis - time
        var line = ""
        while({(line = in.readLine); line != null}) {
            console("<-" + line)

            val msgToken = parser.parseLine(line.trim)

            val command = msgToken.command.toUpperCase

            val time = System.currentTimeMillis
            val reply = Commander.execute(msgToken)
            val timeDelta = timeSince(time)
            console("dT ("+command+"): " + timeDelta)

            //TODO: Handle parser error
            user.send(reply.get)
            Thread.sleep(50)
        }

        //TODO: "un-register" user
        UserModel.delete(user.id)
    }

    def send(msg: String) {
        //don't send empty messages.
        if (!msg.trim.isEmpty) {
            out.println(msg)
            console("-> " + msg)
        }
    }

    def console(msg: CharSequence) {
        Console.println(msg)
    }
}