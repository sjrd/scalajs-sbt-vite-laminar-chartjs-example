package testvite

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

import com.raquo.laminar.api.L.{*, given}

import org.scalajs.dom

object Main {
  final class DataItemID

  case class DataItem(id: DataItemID, label: String, value: Double)

  object DataItem {
    def apply(): DataItem = DataItem(DataItemID(), "?", Math.random())
  }

  val dataVar = Var[List[DataItem]](List(DataItem(DataItemID(), "one", 1.0)))
  val dataSignal = dataVar.signal
  val allValues = dataSignal.map(_.map(_.value))

  def main(args: Array[String]): Unit = {
    // Laminar initialization
    renderOnDomContentLoaded(dom.document.querySelector("#app"), appElement())
  }

  def appElement() = {
    div(
      h1("Hello Vite!"),
      renderDataTable(),
      ul(
        li("Sum of values: ", child.text <-- allValues.map(_.sum)),
        li("Average value: ", child.text <-- allValues.map(vs => vs.sum / vs.size)),
      ),
      renderDataGraph(),
    )
  }

  def renderDataTable() = {
    table(
      thead(
        tr(th("Label"), th("Value"), th("Action")),
      ),
      tbody(
        children <-- dataSignal.split(_.id) { (id, initial, itemSignal) =>
          renderDataItem(id, itemSignal)
        }
      ),
      tfoot(
        tr(td(button("➕", onClick --> (_ => dataVar.update(data => data :+ DataItem()))))),
      ),
    )
  }

  def renderDataItem(id: DataItemID, item: Signal[DataItem]) = {
    val labelUpdater = dataVar.updater[String] { (data, newLabel) =>
      data.map(item => if item.id == id then item.copy(label = newLabel) else item)
    }

    val valueUpdater = dataVar.updater[Double] { (data, newValue) =>
      data.map(item => if item.id == id then item.copy(value = newValue) else item)
    }

    tr(
      td(inputForString(item.map(_.label), labelUpdater)),
      td(inputForDouble(item.map(_.value), valueUpdater)),
      td(button("🗑️", onClick --> (_ => dataVar.update(data => data.filter(_.id != id))))),
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

  def renderDataGraph() = {
    import typings.chartJs.mod.*

    var optChart: Option[Chart] = None

    canvas(
      width := "100%",
      height := "500px",

      onMountCallback { nodeCtx =>
        val ctx = nodeCtx.thisNode.ref
        val chart = Chart.apply.newInstance2(ctx, new ChartConfiguration {
          `type` = ChartType.bar
          data = new ChartData {
            datasets = js.Array(new {
              label = "Value"
              borderWidth = 1
            })
          }
          options = new ChartOptions {
            scales = new ChartScales {
              yAxes = js.Array(new CommonAxe {
                ticks = new TickOptions {
                  beginAtZero = true
                }
              })
            }
          }
        })
        optChart = Some(chart)
      },

      dataSignal --> { data =>
        for (chart <- optChart) {
          chart.data.labels = data.map(_.label).toJSArray
          chart.data.datasets.get(0).data = data.map(_.value).toJSArray
          chart.update()
        }
      },
    )
  }
}
