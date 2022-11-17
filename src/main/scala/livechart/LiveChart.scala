package livechart

import scala.util.Random

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

  case class DataItem(id: DataItemID, label: String, price: Double, count: Int):
    def fullPrice: Double = price * count

  object DataItem:
    def apply(): DataItem =
      DataItem(DataItemID(), "?", Random.nextDouble(), Random.nextInt(5) + 1)
  end DataItem

  type DataList = List[DataItem]

  val dataVar: Var[DataList] = Var(List(DataItem(DataItemID(), "one", 1.0, 1)))
  val dataSignal = dataVar.signal

  def addDataItem(item: DataItem): Unit =
    dataVar.update(data => data :+ item)

  def removeDataItem(id: DataItemID): Unit =
    dataVar.update(data => data.filter(_.id != id))

  def appElement(): Element =
    div(
      h1("Live Chart"),
      renderDataTable(),
      renderDataList(),
      renderDataChart(),
    )
  end appElement

  def renderDataTable(): Element =
    table(
      thead(tr(th("Label"), th("Price"), th("Count"), th("Full price"), th("Action"))),
      tbody(
        children <-- dataSignal.split(_.id) { (id, initial, itemSignal) =>
          renderDataItem(id, itemSignal)
        },
      ),
      tfoot(tr(
        td(button("➕", onClick --> (_ => addDataItem(DataItem())))),
        td(),
        td(),
        td(child.text <-- dataSignal.map(data => "%.2f".format(data.map(_.fullPrice).sum))),
      )),
    )
  end renderDataTable

  def renderDataItem(id: DataItemID, itemSignal: Signal[DataItem]): Element =
    tr(
      td(
        inputForString(
          itemSignal.map(_.label),
          makeDataItemUpdater(id, { (item, newLabel) =>
            item.copy(label = newLabel)
          })
        )
      ),
      td(
        inputForDouble(
          itemSignal.map(_.price),
          makeDataItemUpdater(id, { (item, newPrice) =>
            item.copy(price = newPrice)
          })
        )
      ),
      td(
        inputForInt(
          itemSignal.map(_.count),
          makeDataItemUpdater(id, { (item, newCount) =>
            item.copy(count = newCount)
          })
        )
      ),
      td(
        child.text <-- itemSignal.map(item => "%.2f".format(item.fullPrice))
      ),
      td(button("🗑️", onClick --> (_ => removeDataItem(id)))),
    )
  end renderDataItem

  def makeDataItemUpdater[A](id: DataItemID,
      f: (DataItem, A) => DataItem): Observer[A] =
    dataVar.updater { (data, newValue) =>
      data.map { item =>
        if item.id == id then f(item, newValue) else item
      }
    }
  end makeDataItemUpdater

  def inputForString(valueSignal: Signal[String],
      valueUpdater: Observer[String]): Input =
    input(
      typ := "text",
      value <-- valueSignal,
      onInput.mapToValue --> valueUpdater,
    )
  end inputForString

  def inputForDouble(valueSignal: Signal[Double],
      valueUpdater: Observer[Double]): Input =
    val strValue = Var[String]("")
    input(
      typ := "text",
      value <-- strValue.signal,
      onInput.mapToValue --> strValue,
      valueSignal --> strValue.updater[Double] { (prevStr, newValue) =>
        if prevStr.toDoubleOption.contains(newValue) then prevStr
        else newValue.toString
      },
      strValue.signal --> { valueStr =>
        valueStr.toDoubleOption.foreach(valueUpdater.onNext)
      },
    )
  end inputForDouble

  def inputForInt(valueSignal: Signal[Int],
      valueUpdater: Observer[Int]): Input =
    input(
      typ := "text",
      controlled(
        value <-- valueSignal.map(_.toString),
        onInput.mapToValue.map(_.toIntOption).collect {
          case Some(newCount) => newCount
        } --> valueUpdater,
      ),
    )
  end inputForInt

  def renderDataList(): Element =
    ul(
      children <-- dataSignal.split(_.id) { (id, initial, itemSignal) =>
        li(child.text <-- itemSignal.map(item => s"${item.count} ${item.label}"))
      }
    )
  end renderDataList

  /** Chart.js configuration for the bar chart. */
  val chartConfig =
    import typings.chartJs.mod.*
    new ChartConfiguration {
      `type` = ChartType.bar
      data = new ChartData {
        datasets = js.Array(
          new ChartDataSets {
            label = "Price"
            borderWidth = 1
            backgroundColor = "green"
          },
          new ChartDataSets {
            label = "Full price"
            borderWidth = 1
            backgroundColor = "blue"
          }
        )
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
    }
  end chartConfig

  def renderDataChart(): Element =
    import scala.scalajs.js.JSConverters.*
    import typings.chartJs.mod.*

    var optChart: Option[Chart] = None

    canvas(
      // Regular properties of the canvas
      width := "100%",
      height := "200px",

      // onMountUnmount callback to bridge the Laminar world and the Chart.js world
      onMountUnmountCallback(
        // on mount, create the `Chart` instance and store it in optChart
        mount = { nodeCtx =>
          val domCanvas: dom.HTMLCanvasElement = nodeCtx.thisNode.ref
          val chart = Chart.apply.newInstance2(domCanvas, chartConfig)
          optChart = Some(chart)
        },
        // on unmount, destroy the `Chart` instance
        unmount = { thisNode =>
          for (chart <- optChart)
            chart.destroy()
          optChart = None
        }
      ),

      // Bridge the FRP world of dataSignal to the imperative world of the `chart.data`
      dataSignal --> { data =>
        for (chart <- optChart) {
          chart.data.labels = data.map(_.label).toJSArray
          chart.data.datasets.get(0).data = data.map(_.price).toJSArray
          chart.data.datasets.get(1).data = data.map(_.fullPrice).toJSArray
          chart.update()
        }
      },
    )
  end renderDataChart
end Main
