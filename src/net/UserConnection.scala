package net

import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import io.BufferedSource
import actors.Actor
import messages.{Message}
import targets.User
import messages.parsers.MessageParser
import commands.Commander

//TODO: Server will have actor which handles messages from the clients
class UserConnection(socket: Socket) extends Thread {
    private val out = new PrintStream(socket.getOutputStream)
    private val in  = new BufferedReader(new InputStreamReader(socket.getInputStream))
    private val user = new User(this)
    private val parser = new MessageParser(user)

    override def run() {
        //TODO: "register" user
        var line = ""
        while({(line = in.readLine); line != null}) {
            console("received: " + line)
            val reply = Commander.execute(parser.parseLine(line.trim)).get

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