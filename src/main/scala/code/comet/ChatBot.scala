package code.comet

import net.liftweb.common.Full
import code.util.Coords
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._

class ChatBot extends ChatUser {

  this.coords = Full(Coords(9.193994113713475,-150.47119175000006))
  this.color = "#00B7FF"

  private def init() {
     ChatServer ! NewConnection(this)
    Schedule.schedule(sayLiftWeb, 0)
  }

  private def sayLiftWeb() {
    ChatServer ! Message(this, "http://www.liftweb.net/")
    Schedule.schedule(sayLiftWeb, 60 seconds)
  }

  override def lowPriority = {
    case _ => //gulp
  }

  init()
}
