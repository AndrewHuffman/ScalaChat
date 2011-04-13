package messages

import util.parsing.combinator._
import java.util.Scanner
import targets.User

case class Prefix
case class ServerName(server: String) extends Prefix
case class UserInfo(nick: String, user: String, host: String) extends Prefix
case class Params(list: List[String], tail: String)
case class MessageData(user:User, prefix:Option[Prefix], command:String, params:Option[Params])

trait PrefixParser extends JavaTokenParsers {
    def nickname: Parser[String] = """[\w][\w\-\[\]\\\`\^\{\}\|]*""".r
    def username: Parser[String] = """[^\s@]+""".r
    def hostname: Parser[String] = """[\w\.]+\w+""".r
    def prefix: Parser[Prefix] =
        ((nickname~"!"~username~"@"~hostname ^^ {
            case nick~"!"~user~"@"~host => UserInfo(nick, user, host)
        }) |(hostname ^^ {case host => ServerName(host)})
    )
}

//Scala Parsers aren't thread safe?!?!!??!
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
    def params: Parser[Params] = rep(param)~opt(":"~tail) ^^ {
            case params~None => Params(params, "")
            case params~Some(":"~tail) => Params(params,tail)
        }
    def param: Parser[String] = """[^:][\S]*""".r
    def tail: Parser[String] = """.*$""".r
    def letter: Parser[String] = """[A-Za-z]""".r
}

    //val host: MessageParser[Any] = validIpAddress | validHostname ^^ (x=> {println("host: " +x)})
    //val validIpAddress: MessageParser[Any] = """"(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])""".r ^^ (x=> {println("ip: " +x)})
    //val validHostname: MessageParser[Any] = """"(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])""".r ^^ (x=> {println("hostname: " +x)})

//
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