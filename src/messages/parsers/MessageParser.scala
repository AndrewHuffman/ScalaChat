package messages.parsers

import util.parsing.combinator._
import db.User
import messages.Message


//TODO: rename ParamterParser
trait CommandParser extends TargetsParser {
    lazy val tail: Parser[Tail] = ":"~anything ^^ {
        case ":"~anything => Tail(anything)
    }

    lazy val nonWhiteSpace: Parser[String] = """\S+""".r

    /***
     * Greedy parser!
     */
    lazy val anything: Parser[String] = """.*""".r
}

trait TargetsParser extends RegexParsers {
    //TODO: Rewrite so that it can be nickname || *
    lazy val usermask: Parser[UserMask] = nickname~"!"~username~"@"~hostname ^^ {
        case nick~"!"~user~"@"~host => UserMask(Some(nick),Some(user),Some(host))
    }
    lazy val nickname: Parser[NickName] = """[\w][\w\-\[\]\\\`\^\{\}\|]*""".r ^^ {
        case nick => NickName(nick)
    }
    lazy val username: Parser[UserName] = """[^\s@]+""".r ^^ {
        case user => UserName(user)
    }
    lazy val hostname: Parser[HostName] = """[\w\.]+\w+""".r ^^ {
        case host => HostName(host)
    }
    lazy val channel: Parser[ChannelName] = """[#|&][^,\s]+""".r ^^ {
        case chan => ChannelName(chan)
    }
}

trait PrefixParser extends TargetsParser {
    lazy val prefix: Parser[Prefix] = usermask | (hostname ^^ {case host => ServerName(host)})
}

//Scala Parsers aren't thread safe.
class MessageParser(user :User) extends PrefixParser {
    def parseLine(line :CharSequence):Message = {
        new Message(parseAll(message, line).get)
    }

    lazy val message: Parser[MessageData] = opt(":"~prefix)~command~opt(params) ^^ {
            case Some(":"~prefix)~command~Some(params) => MessageData(user, Some(prefix), command, Some(params))
            case None~command~Some(params) => MessageData(user, None, command, Some(params))
            case Some(":"~prefix)~command~None => MessageData(user, Some(prefix), command, None)
            case None~command~None => MessageData(user, None, command, None)
        }

    lazy val command: Parser[String] = """([A-Za-z]+)|([0-9]{3})""".r
    lazy val params: Parser[Params] = """.*$""".r ^^ (p => Params(p))
    lazy val letter: Parser[String] = """[A-Za-z]""".r
}