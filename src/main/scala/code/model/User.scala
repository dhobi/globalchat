package code.model

import net.liftweb.common.{Empty, Box}
import code.util.Coords
import net.liftweb.http.{CometListener, CometActor}

class User {
  var coords: Box[Coords] = Empty
  var color: String = "#FFFFFF"

  def colorToHexInt = "0x"+color.replace("#","")
  def lat = coords.map(_.lat).openOr(0.0)
  def long = coords.map(_.long).openOr(0.0)
}
