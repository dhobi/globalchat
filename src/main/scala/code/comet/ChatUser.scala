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
import net.liftweb.util.Helpers._
import code.widget.{ColorPicker, Chat, Earth, GoogleMaps}


class ChatUser extends User with CometActor {
  var message = ""

  lazy val id = Helpers.nextFuncName

  val googleMaps = new GoogleMaps("googleMap")
  val earth = new Earth
  val chat = new Chat("messages")
  val colorpicker = new ColorPicker("colorpicker")

  val validations = List((() => this.coords.isDefined, "Please set your location on the map first."))

  override def lowPriority = {
    case NewConnection(user) => partialUpdate(
      earth.createConnection(user.id, user.lat, user.long, user.colorToHexInt) &
        chat.newMessage("New Connection", user.color)
    )
    case ClosedConnection(user) => partialUpdate(
      earth.removeConnection(user.id) &
        chat.newMessage("Connection lost", user.color)
    )
    case Message(user, msg) => partialUpdate(earth.newMessage(user.id, msg))
    case _ => //no no
  }

  override def lifespan = Full(0 seconds)

  def render = {

    def initUserForm = {
      def userForm = {
        ("#colorpicker" #> colorpicker.onChange(this.color, str => {
          this.color = str
          JsCmds.Noop
        }) &
          "#userSubmit" #> generateConnectButton)(Templates(List("templates", "userSetup")).openOr(NodeSeq.Empty))
      }

      def showForm = SetHtml("userform", userForm)

      def onDrop(lat: Double, long: Double) = {
        coords = Full(Coords(lat, long))
        SetHtml("connectButton", generateConnectButton)
      }

      showForm & colorpicker.init & googleMaps.init & googleMaps.initPosition(coords) & googleMaps.markerDrop(onDrop)
    }

    def noWebGL = SetHtml("userform", Templates(List("templates", "nowebgl")).openOr(NodeSeq.Empty))

    def onload = SHtml.ajaxCall(JsRaw("Detector.webgl ? 'true' : 'false'"), str => Helpers.asBoolean(str) match {
      case Full(true) => initUserForm
      case _ => noWebGL
    }).cmd

    ChatServer ! ClosedConnection(this)
    ("#message" #> SHtml.text(message, str => {
      message = str
    }) &
      "#sendMessage" #> SHtml.ajaxSubmit("Send", () => {
        if (message.nonEmpty) {
          ChatServer ! Message(this, message)
        }
        JsCmds.SetValueAndFocus("message", "")
      }))(defaultHtml) ++ Script(OnLoad(onload))
  }

  override def localShutdown() {
    ChatServer ! ClosedConnection(this)
  }

  def generateConnectButton = {

    def initGlobalChat = {
      def initEarth = earth.init & earth.setUserId(id)
      def showChat = JsCmds.JsShowId("chat")
      def closeSetup = JsCmds.JsHideId("userform")

      ChatServer ! NewConnection(this)
      initEarth & showChat & closeSetup
    }

    def getErrors = validations.collect {
      case (validfunc, msg) if !validfunc() => msg
    }

    getErrors match {
      case l if l.isEmpty => SHtml.ajaxButton(Text("Connect"), () => initGlobalChat, "id" -> "userSubmit")
      case l => SHtml.ajaxButton(Text(l.mkString(",")), () => JsCmds.Noop)
    }
  }
}
