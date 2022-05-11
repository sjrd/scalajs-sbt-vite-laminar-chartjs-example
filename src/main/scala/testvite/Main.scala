package testvite

import scala.scalajs.js

object Main {
  def main(args: Array[String]): Unit = {
    js.Dynamic.global.document.querySelector("#app").innerHTML = """
      <h1>Hello Vite!</h1>
      <a href="https://vitejs.dev/guide/features.html" target="_blank">Documentation</a>
    """
  }
}
