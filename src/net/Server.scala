package net
import java.net.ServerSocket
import java.io.IOException
import java.util.{UUID, ArrayList}
import collection.mutable.ArrayBuffer

object Server {
    //private val logger = Logger.getLogger(classOf[Server])
    private val connections = new ArrayBuffer[UserConnection];
    private var stopping = false

    def start() {
        try {
            val serverSocket = new ServerSocket(6667);
            while(!stopping) {
                val connection = new UserConnection(serverSocket.accept)
                connection.start()
                connections.append(connection);
            }
        } catch {
            case e: IOException => Console.err.println(e.getMessage)
        }
    }

    //def getConnection(uuid: UUID) = {
    def getConnection(uuid: String) = {
        connections.find(_.uuid.equals(uuid))
    }

    def stop() {
        stopping = true;
    }
}