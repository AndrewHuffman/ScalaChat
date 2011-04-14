package db

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import dsl.ast.LogicalBoolean

abstract class Model[T](table :Table[T]) {
    val getAllQuery = from(table)(t => select(t))

    def getAll = {
        val qall =
        execute {
            for(row <- getAllQuery) yield row
        }
    }

    def count = {
        val qall = from(table)(t => select(t))
        execute {
            getAllQuery.size
        }
    }

    def insert(row :T):T = execute {
        table.insert(row)
    }

    def insertAll(rows :List[T]) {
        execute {
            table.insert(rows)
        }
    }

    def execute[T](p: => T):T = IRCDB.execute {
        p
    }

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













