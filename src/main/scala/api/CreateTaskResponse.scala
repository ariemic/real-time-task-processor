package api

import domain.{TaskId, TaskStatus}

case class CreateTaskResponse(
                       id: TaskId,
                       status: TaskStatus,
                       result: Option[String],
                       error: Option[String]
                     )


