package examples

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

import scala.concurrent.duration.DurationInt

object ObservableExamples {

  val numbers: Observable[Int] =
    Observable.range(0, 100)
      .mapEval(x => Task(x).delayExecution(1.second))
      .map(_.toInt)



  // Example 1: Basic range with foreach (prints each number)
  def basicRange(): Unit = {
    // Create: Observable.range(0, 5)
    // Do: foreach to print each number
    // Learn: How Observable emits items sequentially
    Observable.range(0,5)
      .foreach(x => print(x))
  }

  // Example 2: Map and filter transformations
  def transformations(): Unit = {
    // Create: Observable.range(0, 10)
    // Do: filter evens, map multiply by 10, foreach print
    // Learn: Basic Observable operators
    Observable.range(0,10)
      .filter( _ % 2 == 0)
      .map(_ * 10)
      .foreach(println)

  }

  // Example 3: Time-based interval
  def intervalExample(): Unit = {
    // Create: Observable.interval(200.millis)
    // Do: take(5), foreach print with timestamp
    // Learn: Time-based emissions
    Observable.interval(200.millis)
      .take(5)
      .foreach(x => println(s"$x at ${System.currentTimeMillis()}"))
  }

  // Example 4: mapEval with Task
  def withTask(): Unit = {
    // Create: Observable.range(0, 3)
    // Do: mapEval(n => Task.sleep(500.millis).map(_ => s"Processed $n"))
    // Do: foreach print
    // Learn: Combining Observable with async Task
    Observable.range(0, 3)
      .mapEval(n => Task.sleep(500.millis)
        .map(_ => s"Processed $n"))
      .foreach(println)

  }

  // Example 5: Buffering
  def buffering(): Unit = {
    // Create: Observable.interval(200.millis).take(10)
    // Do: bufferTimed(1.second), foreach print buffer
    // Learn: Collecting items into batches
    Observable.interval(200.millis)
      .take(10)
      .bufferTimed(1.second)
      .foreach(println)
  }


  def run():Unit = {
    numbers.runAsyncGetLast
  }

}
