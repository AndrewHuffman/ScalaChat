package targets

import db._
import org.squeryl.PrimitiveTypeMode._
import tables.ChannelTable

object Channels extends Model[ChannelTable](IRCDB.channels) {
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

    def create(channelName: String) = execute {
        new Channel(IRCDB.channels.insert(new ChannelTable(name = channelName)))
    }
}