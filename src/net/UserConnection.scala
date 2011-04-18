package net

import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import messages.parsers.MessageParser
import messages.Message
import java.util.UUID
import collection.mutable.ArrayBuffer
import commands.{ReplyBuilder, Commander}
import db.{UserModel, UserTable, IRCDB, User}

//TODO: Server will have actor which handles messages from the clients
class UserConnection(socket: Socket) extends Thread {

    private val out = new PrintStream(socket.getOutputStream)
    private val in  = new BufferedReader(new InputStreamReader(socket.getInputStream))

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



    val host = socket.getInetAddress.getCanonicalHostName

    override def run() {
        def timeSince(time: Long) = System.currentTimeMillis - time
        //TODO: "register" user
        var line = ""
        while({(line = in.readLine); line != null}) {
            console("received: " + line)

            val msgToken = parser.parseLine(line.trim)

            val command = msgToken.command.toUpperCase

            val time = System.currentTimeMillis
            val reply = Commander.execute(msgToken)
            val timeDelta = timeSince(time)
            console("Execution time ("+command+"): " + timeDelta)

            //TODO: Handle parser error
            sendMsg(reply)
            console("reply: "+reply)
        }
        //TODO: "un-register" user
    }

    def sendMsg(msg: CharSequence) {
        out.println(msg);
    }

    def console(msg: CharSequence) {
        Console.println(msg)
    }
}