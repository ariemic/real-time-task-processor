package adapter.generator

import domain.{TaskGenerator, TaskId}

import java.util.UUID

private class RandomTaskGenerator extends TaskGenerator{

  override def generate(taskId: Option[String] = None): TaskId = TaskId(UUID.randomUUID().toString)
}

object RandomTaskGenerator:
  val instance: TaskGenerator = new RandomTaskGenerator
