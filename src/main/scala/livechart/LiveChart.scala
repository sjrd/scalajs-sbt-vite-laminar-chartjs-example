package livechart

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.{*, given}

import org.scalajs.dom

@main
def LiveChart(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Main.appElement()
  )
end LiveChart

object Main:
  final class DataItemID

  case class DataItem(id: DataItemID, label: String, value: Double)

  object DataItem:
    def apply(): DataItem = DataItem(DataItemID(), "?", Math.random())
  end DataItem

  type DataList = List[DataItem]

  val dataVar: Var[DataList] = Var(List(DataItem(DataItemID(), "one", 1.0)))
  val dataSignal = dataVar.signal

  def addDataItem(item: DataItem): Unit =
    dataVar.update(data => data :+ item)

  def removeDataItem(id: DataItemID): Unit =
    dataVar.update(data => data.filter(_.id != id))

  def appElement(): Element =
    div(
      h1("Live Chart"),
      renderDataTable(),
    )
  end appElement

  def renderDataTable(): Element =
    table(
      thead(tr(th("Label"), th("Value"), th("Action"))),
      tbody(
        children <-- dataSignal.split(_.id) { (id, initial, itemSignal) =>
          renderDataItem(id, itemSignal)
        },
      ),
      tfoot(tr(td(button("+", onClick --> (_ => addDataItem(DataItem())))))),
    )
  end renderDataTable

  def renderDataItem(id: DataItemID, itemSignal: Signal[DataItem]): Element =
    tr(
      td(child.text <-- itemSignal.map(_.label)),
      td(child.text <-- itemSignal.map(_.value)),
      td(button("🗑️", onClick --> (_ => removeDataItem(id)))),
    )
  end renderDataItem
end Main
