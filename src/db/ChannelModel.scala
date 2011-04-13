package db

import db._
import org.squeryl.PrimitiveTypeMode._

object ChannelModel extends Model[ChannelTable](IRCDB.channels) {
    def get(name: String) = execute {
        getWhereFirst(c => c.name === name)
    }

    def get(id: Long) = execute {
        IRCDB.channels.lookup(id)
    }

    def exists(id: Long) = execute {
        !get(id).isEmpty
    }

    def exists(channelName: String) = execute {
        !get(channelName).isEmpty
    }
}