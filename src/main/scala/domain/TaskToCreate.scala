package domain

import scala.concurrent.duration.FiniteDuration

case class TaskToCreate(
  taskType: String,
  duration: FiniteDuration
)

