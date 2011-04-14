import commands.{Commander, DefaultCommands}
import db.IRCDB
import net.Server

object IRCServer {
   def main(args: Array[String]) {
       IRCDB //Initialize database session
       Commander.registerCommands(DefaultCommands) //Initialize default commands
       (new Server).start //Initialize Server
   }
}