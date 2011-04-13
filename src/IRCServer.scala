import db.IRCDB
import net.Server

object IRCServer {
   def main(args: Array[String]) {
       IRCDB.init //Initialize database session
       (new Server).start //Initialize Server
   }
}