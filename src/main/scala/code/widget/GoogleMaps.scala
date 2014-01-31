package code.widget

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.util.Helpers
import net.liftweb.http.SHtml
import code.util.Coords
import net.liftweb.common.{Box, Full}
import net.liftweb.http.js.{JsCmd, JsCmds}

/**
 * Small scala wrapper around googlemaps.js
 */

class GoogleMaps(domElem: String) {

  private lazy val globalVariableName = "map_" + Helpers.nextFuncName

  def init = JsRaw(globalVariableName + " = new GoogleMaps('" + domElem + "')").cmd

  def initPosition(coords: Box[Coords]) = coords match {
    case Full(Coords(lat, long)) => JsRaw(globalVariableName+".setPosition(new google.maps.LatLng(" + lat + "," + long + "))").cmd
    case _ => JsCmds.Noop
  }

  def markerDrop(onDrop: (Double, Double) => JsCmd) = {

    def callback(forValue: String, markerFunc: (Double, Double) => JsCmd) = SHtml.ajaxCall(JsRaw(forValue), str => {
      Helpers.tryo {
        val splitted = str.split(",")
        (splitted.head.toDouble, splitted.last.toDouble)
      } match {
        case Full((lat, long)) => markerFunc(lat, long)
        case _ => JsCmds.Noop
      }
    })

    JsRaw(globalVariableName + """.registerMarkerCallback(function(event) {
      var lat = event.latLng.lat();
      var lng = event.latLng.lng();
      var value = lat+","+lng;
      """ + callback("value", onDrop).toJsCmd + """})""").cmd
  }
}
