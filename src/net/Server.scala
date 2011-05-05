package net
import java.net.ServerSocket
import java.io.IOException
import java.util.{UUID, ArrayList}
import collection.mutable.ArrayBuffer

/**
 * Creates the ServerSocket and allows users to connect to the server
 * Also allows ability to retrieve connections based on the
 * connection UUID
 */
object Server {
    //private val logger = Logger.getLogger(classOf[Server])
    private val connections = new ArrayBuffer[UserConnection];
    private var stopping = false
    /**
     * Starts the server and creates a ServerSocket that
     * will accept incoming connections. These connections
     * are maintained in a list.
     *
     * TODO: Clean up in the event of an exception
     */
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

    //TODO: This is smelly code. There should be a better way of
    //finding the connection that is associated with a user record
    def getConnection(uuid: String) = {
        connections.find(_.uuid.equals(uuid))
    }

    /**
     * Stops the server
     */
    def stop() {
        stopping = true;
    }
}