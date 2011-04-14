package commands

import collection.mutable.ArrayBuffer

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 4/13/11
 * Time: 7:49 PM
 * To change this template use File | Settings | File Templates.
 */

abstract class AbstractCommandSet {
    private val _commands = new ArrayBuffer[AbstractCommand]

    //private def _addCommand()
    //use a map

    protected def command(command: AbstractCommand) = {

    }
}