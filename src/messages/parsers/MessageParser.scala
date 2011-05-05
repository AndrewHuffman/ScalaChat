package messages.parsers

import util.parsing.combinator._
import messages.{UserMessage, Message}
import targets.User

/**
 * Parsers that match common command parameters
 */
trait ParameterParser extends TargetsParser {
    /**
     * Parser that match any tail section of a message
     */
    lazy val tail: Parser[Tail] = ":"~anything ^^ {
        case ":"~anything => Tail(anything)
    }
    /**
     * Parser that will match any non white space collection
     * of characters
     */
    lazy val nonWhiteSpace: Parser[String] = """\S+""".r

    /***
     * Greedy parser that will match the end of the string
     */
    lazy val anything: Parser[String] = """.*""".r
}


/**
 * Parsers that match common targets (i.e, nicknames, channels, usermasks, etc)
 *
 * TODO: Return Channel or User objects instead.
 */
trait TargetsParser extends RegexParsers {
    //TODO: Rewrite so that it can be nickname || *
    /**
     * Parser that matches a correctly formatted usermask of the form:
     * nick!user@host
     */
    lazy val usermask: Parser[UserMask] = nickname~"!"~username~"@"~hostname ^^ {
        case nick~"!"~user~"@"~host => UserMask(Some(nick),Some(user),Some(host))
    }
    /**
     * Parser that matches a correctly formatted nickname
     */
    lazy val nickname: Parser[NickName] = """[\w][\w\-\[\]\\\`\^\{\}\|]*""".r ^^ {
        case nick => NickName(nick)
    }
    /**
     * Parser that matches a correctly formatted username
     */
    lazy val username: Parser[UserName] = """[^\s@]+""".r ^^ {
        case user => UserName(user)
    }
    /**
     * Very simple parser for matching hostnames.
     * It is possible for this to match non-hostnames, however,
     * the regular expression for real host names is very complex
     * and isn't regular.
     *
     * This will match any word character (A-Za-z0-9_) that contains atleast
     * one "."
     */
    lazy val hostname: Parser[HostName] = """[\w\.]+\w+""".r ^^ {
        case host => HostName(host)
    }
    /**
     * Matches a correctly formatted channel string, must start
     * with a # or & and does not contain any commas or spaces
     */
    lazy val channel: Parser[ChannelName] = """[#|&][^,\s]+""".r ^^ {
        case chan => ChannelName(chan)
    }
}

/**
 * Parser for the prefix of a message
 */
trait PrefixParser extends TargetsParser {
    /**
     * Matches a correctly formatted prefix:
     * either a hostname (for server originating messages), or usermask
     */
    lazy val prefix: Parser[Prefix] = usermask | (hostname ^^ {case host => ServerName(host)})
}

/**
 * Parser outcome for MessageParser
 */
abstract class Outcome[T]
/* Parser completed successfully with no errors */
case class ParseSuccess[T](t: T) extends Outcome[T]
/* Parser did not complete successfully and input likely contains errors*/
case class ParseFailure[T]() extends Outcome[T]

/**
 * Provides parsers that parse Messages sent to the user
 * and structures them.
 */
//Scala Parsers aren't thread safe.
class MessageParser(user :User) extends RegexParsers {
    /**
     * Given the line, this parser will attempt to parse it
     * into a structures UserMessage object. If it succeeds it'll return
     * a ParseSuccess case class containing the UserMessage,
     * or ParseFailure otherwise.
     */
    def parseLine(line :CharSequence):Outcome[UserMessage] = {
        val result = parseAll(message, line)
        result match {
            case Success(msgToken, next) => ParseSuccess(msgToken)
            case _ => ParseFailure[UserMessage]()
        }
    }

    /**
     * Message parser, returns a structured UserMessage object that contains
     * The Prefix, command, parameters, and originating user.
     */
    lazy val message: Parser[UserMessage] = command~opt(params) ^^ {
            case command~param => new UserMessage(user, command, param)
        }

    /**
     * Matches correctly formatted commands
     */
    lazy val command: Parser[String] = """([A-Za-z]+)|([0-9]{3})""".r
    /**
     * Matches correctly formatted parameters
     */
    lazy val params: Parser[Params] = """.*$""".r ^^ (p => Params(p))
}
