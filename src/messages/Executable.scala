package messages

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/23/11
 * Time: 7:30 PM
 * To change this template use File | Settings | File Templates.
 */

case class ReplyBuilder(reply:Reply, params: String*) {
    def getMessage():String = {
        val paramsArr = params.toArray
        reply.createMessage(paramsArr)
    }
}
trait Executable {
    def execute():ReplyBuilder
}