package code.comet

import code.model.User
import net.liftweb.http.{Templates, SHtml, CometActor}
import net.liftweb.util.Helpers
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.{OnLoad, Script, SetHtml}
import scala.xml.{NodeSeq, Text}
import net.liftweb.http.js.JsCmds
import code.util.Coords
import net.liftweb.common.Full


class ChatUser extends User with CometActor {
  var message = ""
  lazy val id = Helpers.nextFuncName

  override def lowPriority = {
    case NewConnection(user) =>
      val connection = JsRaw("earth.createConnection('" + user.id + "',{latitude:" + user.latToString + ",longitude:" + user.longToString + "}, " + user.colorToHexInt + ")").cmd
      val chat = JsRaw( """document.getElementById("messages").innerHTML += "<div style='color: """ + user.color + """;'>New Connection</div>"""").cmd
      partialUpdate(connection & chat)
    case ClosedConnection(user) =>
      val connection = JsRaw("earth.removeConnection('" + user.id + "')").cmd
      val chat = JsRaw( """document.getElementById("messages").innerHTML += "<div style='color: """ + user.color + """;'>Connection lost</div>"""").cmd
      partialUpdate(connection & chat)
    case Message(user, msg) => partialUpdate(JsRaw("earth.newMessage('" + user.id + "','" + msg.replace("'", "\\'") + "')").cmd)
    case _ => //no no
  }


  def initUserForm = {
    def callback(forValue: String) = SHtml.ajaxCall(JsRaw(forValue), str => {
      this.coords = Helpers.tryo {
        val splitted = str.split(",")
        Coords(splitted.head.toDouble, splitted.last.toDouble)
      }
    }).toJsCmd

    def initPosition = coords match {
      case Full(Coords(lat, long)) => JsRaw("map.setPosition(new google.maps.LatLng(" + lat + "," + long + "))").cmd
      case _ => JsCmds.Noop
    }

    def markerDrop = {
      JsRaw( """map.registerMarkerCallback(function(event) {
      var lat = event.latLng.lat();
      var lng = event.latLng.lng();
      var value = lat+","+lng;
             """ + callback("value") + """})""").cmd
    }

    def showForm = SetHtml("userform", userForm)

    def initColorPicker = JsRaw( """$("#colorpicker").colorpicker();""").cmd

    def initMaps(elem: String) = JsRaw("map = new GoogleMaps('" + elem + "');").cmd

    showForm & initColorPicker & initMaps("googleMap") & initPosition & markerDrop
  }

  def noWebGL = {

    SetHtml("userform", Templates(List("templates", "nowebgl")).openOr(NodeSeq.Empty))
  }

  def render = {
    ChatServer ! ClosedConnection(this)
    val exp = SHtml.ajaxCall(JsRaw("Detector.webgl ? 'true' : 'false'"), str => Helpers.asBoolean(str) match {
      case Full(true) => initUserForm
      case _ => noWebGL
    })

    val chatNode = ("#message" #> SHtml.text(message, str => {
      message = str
    }) &
      "#sendMessage" #> SHtml.ajaxSubmit("Send", () => {
        if (message.nonEmpty) {
          ChatServer ! Message(this, message)
        }
        JsCmds.SetValueAndFocus("message", "")
      }))(defaultHtml)
    chatNode ++ <div>
      {Script(OnLoad(exp.cmd))}
    </div>
  }

  override def localShutdown() {
    ChatServer ! ClosedConnection(this)
  }

  def initGlobalChat = {
    def initEarth = JsRaw("earth = new Earth()").cmd & JsRaw("earth.setId('" + id + "')").cmd
    def showChat = JsCmds.JsShowId("chat")
    def closeSetup = JsCmds.JsHideId("userform")

    if (this.coords.isDefined) {
      ChatServer ! NewConnection(this)
      initEarth & showChat & closeSetup
    } else {
      JsCmds.Alert("Please set your location on the map first.")
    }
  }

  def userForm = {
    ("#colorpicker" #> SHtml.ajaxText(this.color, str => {
      this.color = str
      JsCmds.Noop
    }) &
      "#userSubmit" #> SHtml.ajaxButton(Text("Connect"), () => initGlobalChat))(Templates(List("templates", "userSetup")).openOr(NodeSeq.Empty))
  }
}
