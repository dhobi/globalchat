package code.comet

import code.model.User
import net.liftweb.http.{SHtml, CometActor}
import net.liftweb.util.Helpers
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{OnLoad, Script, SetHtml}
import scala.xml.Text
import net.liftweb.http.js.JsCmds
import net.liftweb.common.Full
import code.util.Coords
import Helpers._
import java.util.Random


class ChatUser extends User with CometActor {
  var message = ""
  lazy val id = Helpers.nextFuncName

  override def lowPriority = {
    case NewConnection(user) => {
      val connection = JsRaw("earth.createConnection('"+user.id+"',{latitude:"+user.latToString+",longitude:"+user.longToString+"}, "+user.colorToHexInt+")").cmd;
      val chat = JsRaw("""document.getElementById("messages").innerHTML += "<div style='color: red;'>New Connection</div>"""").cmd
      partialUpdate(connection & chat)
    }
    case ClosedConnection(user) => {
      val connection = JsRaw("earth.removeConnection('"+user.id+"')").cmd
      val chat = JsRaw("""document.getElementById("messages").innerHTML += "<div style='color: red;'>Connection lost</div>"""").cmd
      partialUpdate(connection & chat)
    }
    case Message(user, msg) => partialUpdate(JsRaw("earth.newMessage('"+user.id+"','"+msg.replace("'","\\'")+"')").cmd)
    case _ => //no no
  }

  def render = {
    ChatServer ! ClosedConnection(this)
    val exp = SHtml.ajaxInvoke{() => SetHtml("userform", userForm) }

    val chatNode = ("#message" #> SHtml.text(message, str => {
      message = str
    }) &
      "#sendMessage" #> SHtml.ajaxSubmit("Send", () => {
        ChatServer ! Message(this, message)
        JsCmds.SetValueAndFocus("message","")
      }))(defaultHtml)
    chatNode ++ <div syle="z-index:999999">{Script(OnLoad(exp.cmd))}</div>
  }

  private def userForm = {
    SHtml.a(() => {
      this.color = "#FFFFFF"
      this.coords = Full(Coords(randomLat.toDouble,randomLong.toDouble))
      ChatServer ! NewConnection(this)
      JsCmds.JsHideId("userform") & JsRaw("yourId = '"+id+"'").cmd
    }, Text("click me"))
  }

  private def randomLat = randomFloat(-90f, 90.0f)
  private def randomLong = randomFloat(-180f, 180.0f)

  private def randomFloat(minX : Float, maxX : Float) = {
    val rand = new Random
    rand.nextFloat() * (maxX - minX) + minX
  }
}
