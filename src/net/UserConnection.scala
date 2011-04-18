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
    private val tempUuid = UUID.randomUUID.toString
    private val tempUser = new UserTable(0,tempUuid)
    private val row = UserModel.insert(tempUser)
    private val user = new User(this, row.id)
    private val parser = new MessageParser(user)
    private val buff = new ArrayBuffer[Message](2)

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