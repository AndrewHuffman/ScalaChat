package commands

import collection.mutable.HashSet

abstract class AbstractCommandSet {
    private val _commands = new HashSet[AbstractCommandExecutable]

    //private def _addCommand()
    //use a map

    protected def addCommand(command: AbstractCommandExecutable) = {
        _commands.add(command)
    }

    def getCommands = _commands.toList
}