package livechart

import org.scalajs.dom

@main
def LiveChart(): Unit = {
  dom.document.querySelector("#app").innerHTML = """
    <h1>Hello Scala.js and Vite!</h1>
    <a href="https://vitejs.dev/guide/features.html"
      target="_blank">Documentation</a>
  """
}
