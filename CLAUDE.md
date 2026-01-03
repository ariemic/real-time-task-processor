# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Real-Time Task Processor - A Scala 3 learning project focused on Monix for building a real-time task processing system with concurrent workers, backpressure handling, and observable streams.

**Core Purpose**: Learn Monix through implementing a system that accepts tasks via REST API, processes them asynchronously using worker pools, streams results in real-time, and handles backpressure and resource management.

## Build Commands

```bash
# Compile the project
sbt compile

# Run tests
sbt test

# Run the application
sbt run

# Launch Scala REPL with project dependencies
sbt console

# Clean build artifacts
sbt clean

# Create fat JAR
sbt assembly
```

## Tech Stack

- **Scala**: 3.3.7
- **SBT**: 1.11.7
- **Monix**: 3.4.1 (Task, Observable, reactive streams)
- **Udash**: REST API framework
- **Circe**: JSON serialization
- **ScalaTest/MUnit**: Testing

## Architecture

This project implements a multi-layered architecture for async task processing:

### 1. HTTP API Layer (Udash/Akka HTTP)
- `POST /tasks` - Submit new task
- `GET /tasks/:id` - Get task status
- `GET /tasks` - List tasks with filters
- `GET /stream/tasks` - SSE stream of task updates
- `DELETE /tasks/:id` - Cancel task

### 2. Task Scheduler
- Queues incoming tasks
- Distributes to worker pool
- Implements backpressure strategies
- Optional priority queuing

### 3. Worker Pool
- Fixed-size pool of concurrent workers
- Workers process tasks from queue
- Simulates different work types (HTTP calls, file operations, delays)
- Per-worker error recovery

### 4. State Management
- Thread-safe concurrent access to task state
- Observable state changes for real-time updates
- In-memory storage (can be extended to database)

### 5. Streaming Layer
- Broadcasts task updates to subscribed clients
- Buffer management for slow consumers
- Connection lifecycle management

## Key Monix Patterns Used

### Task - Lazy Evaluation
```scala
// Task is lazy - nothing executes until .runToFuture or .runAsync
def processTask(task: Task): Task[Result] =
  Task.sleep(task.duration)
    .flatMap(_ => performWork(task))
    .timeout(30.seconds)
    .onErrorRecoverWith { case e => Task.pure(Failed(e.getMessage)) }
```

**Important**: Task vs Future
- Future is eager (starts immediately on creation)
- Task is lazy (only executes when run)
- Task has cancellation, Future does not
- Task is referentially transparent, Future is not

### Observable - Reactive Streams
```scala
val taskUpdates: Observable[TaskUpdate] =
  Observable.fromReactivePublisher(taskUpdatePublisher)
    .mapEval(update => Task(enrichUpdate(update)))
    .bufferTimed(100.millis, 10)
```

**Key concepts**:
- Hot vs Cold observables
- Backpressure strategies: `OverflowStrategy.DropOld`, `BackPressure`, etc.
- `multicast` for broadcasting to multiple subscribers

### Schedulers
```scala
implicit val io: Scheduler = Scheduler.io("worker-pool")
implicit val compute: Scheduler = Scheduler.computation()

task.executeOn(io).runToFuture
```

- Use `Scheduler.io` for IO-bound work (HTTP calls, file ops)
- Use `Scheduler.computation` for CPU-bound work
- Use `TestScheduler` for deterministic testing

### Concurrent Execution
```scala
Task.parSequence(tasks)      // Run all in parallel
Task.race(task1, task2)       // First to complete
Task.parZip2(task1, task2)    // Both together
Task.parSequenceN(n)(tasks)   // Bounded parallelism
```

### Resource Management
```scala
Task.bracket(acquireResource) { resource =>
  useResource(resource)
} { resource =>
  Task(resource.close())
}
```

Always use `bracket` or `Resource` type for resources like DB connections, HTTP clients, file handles.

### Fiber Management & Cancellation
```scala
for {
  fiber <- task.start           // Returns CancelableFuture
  _ <- Task.sleep(5.seconds)
  _ <- fiber.cancel()
} yield ()
```

### Concurrent State - MVar & Atomic
```scala
// MVar for coordination (blocking semantics)
for {
  mvar <- MVar[Task].empty[Task[Unit]]
  _ <- mvar.put(someTask)       // Blocks if full
  task <- mvar.take             // Blocks if empty
} yield task

// Atomic for lock-free updates
val counter = Atomic(0L)
counter.increment()
```

## Implementation Phases

The README outlines a 5-phase implementation plan:

1. **Phase 1**: Basic Task + Observable understanding
2. **Phase 2**: Worker pool with concurrent task processing
3. **Phase 3**: Real-time streaming with backpressure
4. **Phase 4**: Advanced patterns (cancellation, retry, circuit breaker)
5. **Phase 5**: Testing with TestScheduler

Refer to readme.md for detailed phase descriptions and learning objectives.

## Critical Design Decisions

### Task Queue
- Consider MVar (blocking) vs ConcurrentQueue (non-blocking)
- Bounded vs unbounded queue size
- Priority queue support if needed

### Backpressure Strategy
- Choose between DropOld, Buffer, or Fail
- Decide on per-client vs global buffering
- Communicate backpressure to clients via HTTP 429 or SSE

### Scheduler Configuration
- Separate schedulers for IO vs compute
- Worker pool size: typically CPU count for compute, higher for IO
- Prevent thread starvation

### Error Handling
- Retry transient failures with exponential backoff
- Circuit breaker for cascading failures
- Dead letter queue for permanently failed tasks

## Testing

Use `TestScheduler` for deterministic testing:

```scala
implicit val scheduler = TestScheduler()

val task = Task.sleep(1.hour).map(_ => "done")
val f = task.runToFuture

scheduler.tick(1.hour)
// Now future is complete
```

This allows testing time-dependent code without waiting.

## Common Pitfalls

1. **Blocking on wrong scheduler**: Never block on computation scheduler, use IO scheduler
2. **Forgetting to run Tasks**: Task is lazy, must call `.runToFuture` or `.runAsync`
3. **Resource leaks**: Always use `bracket` or `Resource` for cleanup
4. **Unbounded parallelism**: Use `parSequenceN` instead of `parSequence` for large task lists
5. **Hot Observable without multicast**: Use `.multicast` when multiple subscribers need same events

## Development Workflow

This is a learning project. Focus on:
- Experimenting in `sbt console` to understand Task/Observable behavior
- Reading Monix documentation at https://monix.io/
- Starting small (Phase 1) before building complex features
- Testing async code with TestScheduler
- Understanding flatMap and monadic composition deeply