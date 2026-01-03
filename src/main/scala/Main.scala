import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@main def run(): Unit = {

  // Example 1: Simple Task that prints
  println("=== Example 1: Basic Task ===")
  val simpleTask: Task[String] = Task {
    println("Executing task!")
    "Hello from Task"
  }

  // Notice: Nothing printed yet! Task is LAZY
  println("Task created, but not executed yet")

  // Now let's run it
  val future = simpleTask.runToFuture
  val result = Await.result(future, 5.seconds)
  println(s"Result: $result")


  // Example 2: Task with delay
  println("\n=== Example 2: Task with delay ===")
  val delayedTask: Task[String] = Task.sleep(2.seconds).map { _ =>
    println("After 2 seconds...")
    "Delayed result"
  }

  val delayedResult = Await.result(delayedTask.runToFuture, 5.seconds)
  println(s"Result: $delayedResult")


  // Example 3: flatMap composition
  println("\n=== Example 3: flatMap ===")
  def processStep1(input: String): Task[String] = Task {
    println(s"Step 1: Processing $input")
    s"$input-processed"
  }

  def processStep2(input: String): Task[String] = Task.sleep(1.second).map { _ =>
    println(s"Step 2: Processing $input")
    s"$input-complete"
  }

  val composedTask: Task[String] =
    processStep1("data")
      .flatMap(result => processStep2(result))

  val composedResult = Await.result(composedTask.runToFuture, 5.seconds)
  println(s"Final result: $composedResult")


  // Example 4: for-comprehension (syntactic sugar for flatMap)
  println("\n=== Example 4: for-comprehension ===")
  val forCompTask: Task[String] = for {
    step1 <- processStep1("input")
    step2 <- processStep2(step1)
  } yield step2

  val forCompResult = Await.result(forCompTask.runToFuture, 5.seconds)
  println(s"Result: $forCompResult")

  println("\n=== All examples completed! ===")
}