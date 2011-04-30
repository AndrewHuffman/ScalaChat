package targets

import messages.Message

abstract class Target {
    def send(out: String)

    def send(message: Message) {
        send(message.toString)
    }
}