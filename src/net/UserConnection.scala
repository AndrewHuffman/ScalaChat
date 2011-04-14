package net

import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import io.BufferedSource
import actors.Actor
import messages.{Message}
import targets.User
import messages.parsers.MessageParser

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
            Console.println("received: " + line)
            //TODO: Handle parser error
            handleMessage(parser.parseLine(line.trim))
        }
        //TODO: "un-register" user
    }

    def sendMsg(msg: CharSequence) {
        out.println(msg);
    }

    private def handleMessage(msg: Message) {
        out.println(msg.executeCommand.getMessage)
    }

}