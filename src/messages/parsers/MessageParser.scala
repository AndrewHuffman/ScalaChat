package messages.parsers

import util.parsing.combinator._
import messages.{UserMessage, Message}
import targets.User

//TODO: rename ParamterParser
trait CommandParser extends TargetsParser {
    //lazy val mode: Parser[List[Mode]] =

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
class MessageParser(user :User) extends RegexParsers {
    def parseLine(line :CharSequence):UserMessage = {
        parseAll(message, line).get
    }

    lazy val message: Parser[UserMessage] = command~opt(params) ^^ {
            case command~param => new UserMessage(user, command, param)
        }

    lazy val command: Parser[String] = """([A-Za-z]+)|([0-9]{3})""".r
    lazy val params: Parser[Params] = """.*$""".r ^^ (p => Params(p))
}
