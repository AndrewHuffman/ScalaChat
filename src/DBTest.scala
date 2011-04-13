import java.sql.Timestamp
import java.util.Date
import org.scalaquery.ql.Column
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

class Author(val id: Long, val firstName: String, val lastName: String, val email: Option[String]) {
    def this() = this(0,"","",Some(""))
}

// fields can be mutable or immutable

class Book(val id: Long, val title: String, val authorId: Long) {
    def this() = this(0,"",0)
}

object Library extends Schema {
    val authors = table[Author]
    val books = table[Book]

    on(authors)(s => declare(
        s.email      is(unique,indexed),
        s.firstName  is(indexed),
        s.lastName   is(indexed),
        columns(s.firstName, s.lastName) are(indexed)
    ))
}