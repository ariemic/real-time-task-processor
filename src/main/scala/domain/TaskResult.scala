package domain

import domain.{TaskId, TaskStatus}

case class TaskResult(
                       id: TaskId,
                       status: TaskStatus,
                       result: Option[String],
                       error: Option[String]
                     )


