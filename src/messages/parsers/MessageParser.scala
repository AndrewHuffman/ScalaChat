package messages.parsers

import util.parsing.combinator._
import targets.User
import messages.Message
import db.UserModel

case class Prefix
case class ServerName(server: HostName) extends Prefix
case class UserMask(nick: Option[NickName], user: Option[UserName], host: Option[HostName]) extends Prefix
case class Params(params: String)
case class MessageData(user:User, prefix:Option[Prefix], command:String, params:Option[Params])
case class NickName(name:String) {
    def inUse = {
        UserModel.exists(name)
    }
}
case class ChannelName(name:String)
case class HostName(host:String)
case class UserName(name:String)

trait TargetsParser extends RegexParsers {
    //TODO: Rewrite so that it can be nickname || *
    def usermask: Parser[UserMask] = nickname~"!"~username~"@"~hostname ^^ {
        case nick~"!"~user~"@"~host => UserMask(Some(nick),Some(user),Some(host))
    }
    def nickname: Parser[NickName] = """[\w][\w\-\[\]\\\`\^\{\}\|]*""".r ^^ {
        case nick => NickName(nick)
    }
    def username: Parser[UserName] = """[^\s@]+""".r ^^ {
        case user => UserName(user)
    }
    def hostname: Parser[HostName] = """[\w\.]+\w+""".r ^^ {
        case host => HostName(host)
    }
    def channel: Parser[ChannelName] = """[#|&][^,\s]+""".r ^^ {
        case chan => ChannelName(chan)
    }
}

//trait CommandParameterParser extends TargetsParser {
//    def target: Parser[Any] = to~repsep(target,",")
//    def to: Parser[Any] = channel | username~"@"~hostname | nickname //|mask
//}

trait PrefixParser extends TargetsParser {
    def prefix: Parser[Prefix] = usermask | (hostname ^^ {case host => ServerName(host)})
}

//Scala Parsers aren't thread safe.
class MessageParser(user :User) extends PrefixParser {
   def parseLine(line :CharSequence):Message = {
        new Message(parseAll(message, line).get)
    }

    def message: Parser[MessageData] = opt(":"~prefix)~command~opt(params) ^^ {
            case Some(":"~prefix)~command~Some(params) => MessageData(user, Some(prefix), command, Some(params))
            case None~command~Some(params) => MessageData(user, None, command, Some(params))
            case Some(":"~prefix)~command~None => MessageData(user, Some(prefix), command, None)
            case None~command~None => MessageData(user, None, command, None)
        }

    def command: Parser[String] = """([A-Za-z]+)|([0-9]{3})""".r
    def params: Parser[Params] = """.*$""".r ^^ (p => Params(p))
//    def params: Parser[Params] = rep(param)~opt(":"~tail) ^^ {
//            case params~None => Params(params, "")
//            case params~Some(":"~tail) => Params(params,tail)
//        }
//    def param: Parser[String] = """[^:][\S]*""".r
//    def tail: Parser[String] = """.*$""".r
    def letter: Parser[String] = """[A-Za-z]""".r
}

    //val host: MessageParser[Any] = validIpAddress | validHostname ^^ (x=> {println("host: " +x)})
    //val validIpAddress: MessageParser[Any] = """"(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])""".r ^^ (x=> {println("ip: " +x)})
    //val validHostname: MessageParser[Any] = """"(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])""".r ^^ (x=> {println("hostname: " +x)})

// import java.util.Scanner
//object ParserTest extends MessageParser {
//    def main() {
//        val in = new Scanner(System.in)
//        print("> ")
//        while(in.hasNext) {
//            val line = in.nextLine
//            println("line: " + line)
//            println(parseAll(message, line.trim))
//            print(">")
//        }
//    }
//
//    def chars(in: String) {
//        in.foreach((c:Char) => {
//            //print(c+"=>"+c.getNumericValue+" ")
//        })
//        print("\n")
//    }
//}