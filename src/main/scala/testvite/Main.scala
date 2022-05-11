package testvite

import scala.scalajs.js

import com.raquo.laminar.api.L.{*, given}

import org.scalajs.dom

object Main {
  def appElement = div(
    h1("Hello Vite!"),
    a(href := "https://vitejs.dev/guide/features.html", target := "_blank", "Documentation"),
  )

  def main(args: Array[String]): Unit = {
    // Laminar initialization boilerplate
    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.querySelector("#app"), appElement)
    } (unsafeWindowOwner)
  }
}
