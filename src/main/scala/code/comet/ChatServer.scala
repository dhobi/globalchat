package code.comet

import net.liftweb.actor.LiftActor

case class NewConnection(user : ChatUser)
case class ClosedConnection(user : ChatUser)
case class Message(user : ChatUser, message : String)

object ChatServer extends LiftActor {
  var users : List[ChatUser] = Nil

  protected def messageHandler = {
    case n@NewConnection(user) => {
      users.foreach(registeredUser => {
        user ! NewConnection(registeredUser)
      })

      users ::= user
      broadCastMessage(n)
    }
    case n@ClosedConnection(user) => {
      if(users.exists(_.id.equals(user.id))) {
        users = users.filterNot(_.id.equals(user.id))
        broadCastMessage(n)
      }

    }
    case n@Message(user,message) => {
      broadCastMessage(n)
    }
  }

  private def broadCastMessage(msg : Any) {
    users.foreach(_ ! msg)
  }
}
