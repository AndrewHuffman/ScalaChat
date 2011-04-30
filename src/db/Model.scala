package db

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import dsl.ast.LogicalBoolean
import org.apache.log4j.Logger

abstract class Model[T <: KeyedEntity[Long]](protected val table :Table[T]) {
    val getAllQuery = from(table)(t => select(t))
    val logger = Logger.getLogger(classOf[Model[T]])

    def getAll = {
        execute {
            for(row <- getAllQuery) yield row
        }
    }

    def update(record: T) { execute { table.update(record) } }

    def count = execute { getAllQuery.size }

    def insert(row :T):T = execute {
        table.insert(row)
    }

    def insertAll(rows :List[T]) {
        execute {
            table.insert(rows)
        }
    }

    def execute[T](p: => T):T = IRCDB.execute { p }

    def getWhere(whereClause:T => LogicalBoolean) = {
        val query = table.where(whereClause)
        execute {
            for(row <- query) yield row
        }
    }

    def getWhereFirst(whereClause:T => LogicalBoolean) = {
        val query = table.where(whereClause)

        execute {
            query.size match {
                case i if i >= 1 => Some(query.single)
                case _ => None
            }
        }
    }
}













