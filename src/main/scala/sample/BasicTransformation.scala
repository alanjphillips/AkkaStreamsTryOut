package sample

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Outlet}
import akka.stream._
import akka.stream.scaladsl._

object BasicTransformation {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Sys")
    import system.dispatcher

    implicit val materializer = ActorMaterializer()

    val text =
      """|Lorem Ipsum is simply dummy text of the printing and typesetting industry.
        |Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
        |when an unknown printer took a galley of type and scrambled it to make a type
        |specimen book.""".stripMargin



    val g: RunnableGraph[_] = RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        // Source
        val A: Outlet[String] = builder.add(Source.fromIterator(() => text.split("\\s").iterator)).out

        // Flows
        val B: FlowShape[String, String] = builder.add(filterByWord("Ipsum"))

        val C: FlowShape[String, String] = builder.add(convertToUpper)

        val D: FlowShape[String, String] = builder.add(printFilteredUpper)

        // Sinks
        val E: Inlet[Any] = builder.add(Sink.ignore).in

        A ~> B ~> C ~> D ~> E

        ClosedShape
    })

    g.run()
  }

  def filterByWord(fword: String) = Flow[String].filter(_ equalsIgnoreCase fword)

  def convertToUpper = Flow[String].map(_.toUpperCase)

  def printFilteredUpper = Flow[String].map {
    s => println(s); s
  }

}
