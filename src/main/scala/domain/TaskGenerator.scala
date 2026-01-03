package domain

trait TaskGenerator {
  def generate(taskId: Option[String] = None): TaskId
}
