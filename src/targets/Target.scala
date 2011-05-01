package targets

import messages.Message

trait Target {
    def send(out: String)

    def send(message: Message) {
        send(message.toString)
    }
}