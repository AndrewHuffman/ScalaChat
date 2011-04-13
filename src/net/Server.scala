package net
import java.util.ArrayList
import java.net.ServerSocket
import java.io.IOException

class Server {
    //private val logger = Logger.getLogger(classOf[Server])
    private val connections = new ArrayList[UserConnection]();

    def start() {
        try {
            val serverSocket = new ServerSocket(6667);
            val connection = new UserConnection(serverSocket.accept)
            connection.start()
            connections.add(connection);
        } catch {
            case e: IOException => Console.err.println(e.getMessage)
        }
    }
}