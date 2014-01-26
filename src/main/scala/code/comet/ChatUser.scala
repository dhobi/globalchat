package code.comet

import code.model.User
import net.liftweb.http.{SHtml, CometActor, CometListener}
import net.liftweb.util.{Helpers, ClearClearable}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{OnLoad, Script, SetHtml}
import scala.xml.Text
import net.liftweb.http.js.JsCmds
import net.liftweb.common.Full
import code.util.Coords


class ChatUser extends User with CometActor {

  lazy val id = Helpers.nextFuncName

  override def lowPriority = {
    case NewConnection(user) => partialUpdate(JsRaw("earth.createConnection('"+user.id+"',{latitude:"+user.latToString+",longitude:"+user.longToString+"}, "+user.colorToHexInt+")").cmd)
    case ClosedConnection(user) => JsRaw("earth.removeConnection("+user.id+")")
    case Message(user, message) => JsRaw("earth.newMessage("+user.id+","+message+")")
    case _ => //no no
  }

  def render = {
    val exp = SHtml.ajaxInvoke{() => SetHtml("userform", userForm) }
    <div syle="z-index:999999">{Script(OnLoad(exp.cmd))}</div>
  }

  private def userForm = {
    SHtml.a(() => {
      this.color = "#FFFFFF"
      this.coords = Full(Coords(-9.449062,-54.843750 ))
      ChatServer ! NewConnection(this)
      JsCmds.JsHideId("userform")
    }, Text("click me"))
  }
}
