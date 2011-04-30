package net

import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import messages.parsers.MessageParser
import java.util.UUID
import collection.mutable.ArrayBuffer
import commands.{Commander}
import db.tables.UserTable
import db.{IRCDB}
import messages.{ReplyBuilder, Message}
import targets.User

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
        user.delete()
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