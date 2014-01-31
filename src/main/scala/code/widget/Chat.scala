package code.widget

import net.liftweb.http.js.JE.JsRaw

/**
 * Small scala wrapper around chat window
 */
class Chat(domElem : String) {
   def newMessage(msg : String, color : String) = JsRaw( """$('#"""+domElem+"""').append("<div style='color: """ + color + """;'>"""+msg+"""</div>")""").cmd
}
