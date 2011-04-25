package messages

//TODO: Needs to actually build a Message Object. Message object needs actual toString method

object MessageBuilder {
    def create(prefix: String, command: String, params: Array[String], tail: String):String = {
        //create(prefix, command, )
        val rest = new StringBuilder()
        for(param <- params) {
            rest.append(param)
        }
        rest.append(" ").append(tail)
        create(prefix, command, rest.toString())
    }

    def create(prefix: String, command: String, rest: String):String = {
        val sb = new StringBuilder(":")
        sb.append(prefix).append(" ").append(command).append(" ").append(rest)
        sb.toString()
    }

    def create(command: String, rest: String):String = {
        create("", command, rest)
    }
}