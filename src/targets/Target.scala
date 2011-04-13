package targets

/**
 * Created by IntelliJ IDEA.
 * User: Andrew
 * Date: 4/5/11
 * Time: 2:40 PM
 * To change this template use File | Settings | File Templates.
 */

trait Target {
    def sendMsg(msg:String)
    def getName():String
}