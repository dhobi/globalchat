package code.widget

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.SHtml

/**
 * Small wrapper around color picker
 */
class ColorPicker(domElem : String) {
  def init = JsRaw( """$("#"""+domElem+"""").colorpicker();""").cmd

  def onChange(oldValue : String, change : String => JsCmd) = SHtml.ajaxText(oldValue ,change)
}
