import commands.{Commander, DefaultCommands}
import db.IRCDB
import net.Server

object IRCServer {
   def main(args: Array[String]) {
       println("Initializing Database...")
       IRCDB.init //Initialize database session
       println("Adding Commands...")
       Commander.registerCommands(DefaultCommands) //Initialize default commands
       println("Starting Server...")
       (new Server).start() //Initialize Server
   }
}