package commands

import collection.mutable.HashSet

abstract class AbstractCommandSet {
    private val _commands = new HashSet[AbstractParameterCommand[_]]

    //private def _addCommand()
    //use a map

    protected def addCommand(command: AbstractParameterCommand[_]) = {
        _commands.add(command)
    }

    def getCommands = _commands.toList
}