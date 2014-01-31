package code.widget

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.util.Helpers

/**
 * Small scala wrapper around earth.js
 */
class Earth {

  private lazy val globalVariableName = "earth_" + Helpers.nextFuncName

  def init = JsRaw(globalVariableName + " = new Earth()").cmd

  def setUserId(id: String) = JsRaw(globalVariableName + ".setId('" + id + "')").cmd

  def createConnection(id: String, lat: Double, long: Double, color: String) = JsRaw(globalVariableName + ".createConnection('" + id + "',{latitude:" + lat + ",longitude:" + long + "}, " + color + ")").cmd

  def removeConnection(id: String) = JsRaw(globalVariableName + ".removeConnection('" + id + "')").cmd

  def newMessage(id: String, msg: String) = JsRaw(globalVariableName + ".newMessage('" + id + "','" + msg.replace("'", "\\'") + "')").cmd
}
