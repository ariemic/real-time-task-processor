package adapter.generator

import domain.{TaskGenerator, TaskId}

class FixedTaskGenerator extends TaskGenerator{

  override def generate(taskId: Option[String] = None): TaskId = TaskId(taskId.get)
}
