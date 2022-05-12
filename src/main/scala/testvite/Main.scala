package testvite

import scala.scalajs.js

import com.raquo.laminar.api.L.{*, given}

import org.scalajs.dom

object Main {
  final class DataItemID

  case class DataItem(id: DataItemID, label: String, value: Double)

  object DataItem {
    def apply(): DataItem = DataItem(DataItemID(), "?", Math.random())
  }

  val data = Var[List[DataItem]](List(DataItem(DataItemID(), "one", 1.0)))
  val allValues = data.signal.map(_.map(_.value))

  def appElement = div(
    h1("Hello Vite!"),
    renderDataTable(),
    ul(
      li("Sum of values: ", child.text <-- allValues.map(_.sum)),
      li("Average value: ", child.text <-- allValues.map(vs => vs.sum / vs.size)),
    ),
  )

  def renderDataTable() = {
    table(
      thead(
        tr(th("Label"), th("Value"), th("Action")),
      ),
      tbody(
        children <-- data.signal.split(_.id) { (id, initial, itemSignal) =>
          renderDataItem(id, itemSignal)
        }
      ),
      tfoot(
        tr(td(button("➕", onClick --> (_ => data.update(data => data :+ DataItem()))))),
      ),
    )
  }

  def renderDataItem(id: DataItemID, item: Signal[DataItem]) = {
    val labelUpdater = data.updater[String] { (data, newLabel) =>
      data.map(item => if item.id == id then item.copy(label = newLabel) else item)
    }

    val valueUpdater = data.updater[Double] { (data, newValue) =>
      data.map(item => if item.id == id then item.copy(value = newValue) else item)
    }

    tr(
      td(inputForString(item.map(_.label), labelUpdater)),
      td(inputForDouble(item.map(_.value), valueUpdater)),
      td(button("🗑️", onClick --> (_ => data.update(data => data.filter(_.id != id))))),
    )
  }

  def inputForString(valueSignal: Signal[String], valueUpdater: Observer[String]): Input = {
    input(
      typ := "text",
      controlled(
        value <-- valueSignal,
        onInput.mapToValue --> valueUpdater,
      ),
    )
  }

  def inputForDouble(valueSignal: Signal[Double], valueUpdater: Observer[Double]): Input = {
    val strValue = Var[String]("")
    input(
      typ := "text",
      controlled(
        value <-- strValue.signal,
        onInput.mapToValue --> strValue,
      ),
      valueSignal --> strValue.updater[Double] { (prevStr, newValue) =>
        if prevStr.toDoubleOption.contains(newValue) then prevStr
        else newValue.toString
      },
      strValue.signal --> { valueStr =>
        valueStr.toDoubleOption.foreach(valueUpdater.onNext)
      },
    )
  }

  def main(args: Array[String]): Unit = {
    // Laminar initialization boilerplate
    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.querySelector("#app"), appElement)
    } (unsafeWindowOwner)
  }
}
