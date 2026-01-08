package domain

import monix.eval.Task
import scala.concurrent.duration._

/**
 * Domain service for processing tasks
 * Works only with domain entities, no API dependencies
 */
class TaskProcessor(taskGenerator: TaskGenerator) {

  def processTask(request: TaskToCreate): Task[TaskResult] = {
    val taskId = taskGenerator.generate()

    Task.sleep(request.duration)  // duration is already FiniteDuration
      .map(_ => TaskResult(taskId, TaskStatus.Completed, Some("success"), None))
      .timeout(30.seconds)
      .onErrorHandle(error =>
        TaskResult(taskId, TaskStatus.Failed, None, Some(error.getMessage))
      )
  }

}
