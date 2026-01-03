package api

case class CreateTaskRequest(
                        taskType: String,
                        duration: Int // milliseconds
                      )

