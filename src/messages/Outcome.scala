package messages

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 4/5/11
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */

case class Outcome
case class Failure(info: Any) extends Outcome
case class Success(info: Any) extends Outcome