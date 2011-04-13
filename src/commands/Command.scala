package commands

import messages._

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 3/24/11
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */

abstract class Command(srcMsg: Message) extends Executable

trait ValidParams {
    def assertParamLength(params: Params, minSize: Int, requireTail: Boolean) = {
        (params.list.length < minSize) match {
            case false => false
            case true if requireTail => {
                if (params.tail.trim.isEmpty) false
                else true
            }
            case _ => true
        }
    }
}