# Real-Time Task Processor - Monix Learning Project

A practical Scala project focused on learning Monix through building a real-time task processing system with concurrent workers, backpressure handling, and observable streams.

## Project Overview

This project will teach you core Monix concepts through implementing a system that:
- Accepts tasks via REST API
- Processes them asynchronously using worker pools
- Streams results back in real-time
- Handles backpressure and resource management
- Provides monitoring and metrics

**Why this project?** It naturally demonstrates Monix's strengths: Task composition, Observable streams, concurrent execution, and resource safety - all concepts you'll use in production code.

## Functional Requirements

**Core Features**
1. **Task Submission**
    - Accept task requests via REST API
    - Validate task parameters
    - Queue tasks for processing
    - Return task ID immediately (non-blocking)

2. **Async Task Processing**
    - Process tasks in background worker pool
    - Support different task types (simulated work, HTTP calls, file operations)
    - Configurable processing delays and failure rates
    - Cancel running tasks

3. **Real-Time Status Streaming**
    - Stream task status updates via SSE (Server-Sent Events)
    - Track: queued, processing, completed, failed states
    - Monitor active workers and queue depth

4. **Task Results Retrieval**
    - Query task status by ID
    - Get task results once completed
    - List all tasks with filtering (status, time range)

5. **Worker Pool Management** (optional)
    - Configure worker pool size
    - Graceful shutdown with running task completion
    - Worker health monitoring

**Non-functional Requirements**
- Handle 100+ concurrent tasks
- Backpressure handling when overwhelmed
- Resource cleanup (no memory leaks)
- Fast task queuing (< 10ms response)
- Graceful degradation under load

## Technical Architecture

**Components You'll Build**

1. **HTTP API Layer** (Udash/Akka HTTP)
    - POST /tasks - Submit new task
    - GET /tasks/:id - Get task status
    - GET /tasks - List tasks with filters
    - GET /stream/tasks - SSE stream of all task updates
    - DELETE /tasks/:id - Cancel task

2. **Task Scheduler**
    - Queue incoming tasks
    - Distribute to worker pool
    - Handle backpressure strategies
    - Priority queuing (optional)

3. **Worker Pool**
    - Fixed-size pool of concurrent workers
    - Each worker processes tasks from queue
    - Simulate different types of work
    - Error recovery per worker

4. **State Management**
    - Track all tasks (in-memory or database)
    - Observable state changes
    - Thread-safe concurrent access

5. **Streaming Layer**
    - Broadcast task updates to subscribed clients
    - Buffer management for slow consumers
    - Connection lifecycle management

## Monix Concepts You'll Learn & Use

### 1. **Task - The Heart of Monix**
Use for:
- All async operations (API handlers, task processing, database calls)
- Lazy evaluation with referential transparency
- Resource-safe execution with bracket/guarantee
- Retry logic and timeout handling

```scala
// You'll write code like:
def processTask(task: Task): Task[Result] =
  Task.sleep(task.duration)
    .flatMap(_ => performWork(task))
    .timeout(30.seconds)
    .onErrorRecoverWith { case e => 
      Task.pure(Failed(e.getMessage)) 
    }
```

**Learning Focus**: Unlike Future (eager), Task is lazy and composable. Understand:
- Task construction vs execution
- `.runToFuture` - when side effects actually happen
- Error handling with Task vs try/catch
- Difference between Task, Future, and IO monads

### 2. **Observable - Reactive Streams**
Use for:
- Real-time task status updates
- Server-Sent Events implementation
- Processing streams of incoming tasks
- Backpressure management

```scala
// You'll create streams like:
val taskUpdates: Observable[TaskUpdate] =
  Observable.fromReactivePublisher(taskUpdatePublisher)
    .mapEval(update => Task(enrichUpdate(update)))
    .bufferTimed(100.millis, 10)
```

**Learning Focus**:
- Hot vs Cold observables
- Backpressure strategies (buffer, drop, fail)
- Observable composition and transformation
- Multicast for multiple subscribers

### 3. **Scheduler - Execution Context**
Use for:
- Running Tasks with different characteristics
- IO-bound vs CPU-bound work separation
- Testing with deterministic scheduling

```scala
// You'll configure schedulers:
implicit val io: Scheduler = Scheduler.io("worker-pool")
implicit val compute: Scheduler = Scheduler.computation()

task.executeOn(io).runToFuture
```

**Learning Focus**:
- When to use different scheduler types
- Thread pool configuration
- Avoiding blocking operations on wrong scheduler

### 4. **Concurrent & Parallel Execution**
Use for:
- Running multiple tasks simultaneously
- Race conditions and coordinated execution
- Parallel task processing

```scala
// You'll coordinate tasks:
Task.parSequence(tasks) // Run all in parallel
Task.race(task1, task2) // First to complete wins
Task.parZip2(task1, task2) // Both together
```

**Learning Focus**:
- parSequence vs sequence (parallel vs sequential)
- gather vs parSequence (error handling differences)
- Bounded parallelism with parSequenceN

### 5. **Resource Management**
Use for:
- Database connections
- HTTP clients
- File handles
- Worker pool lifecycle

```scala
// You'll use bracket pattern:
Task.bracket(acquireResource) { resource =>
  useResource(resource)
} { resource =>
  Task(resource.close())
}
```

**Learning Focus**:
- Bracket for acquisition/release
- Resource type for composing resources
- Preventing resource leaks

### 6. **Fiber Management**
Use for:
- Cancellable background work
- Task cancellation
- Structured concurrency

```scala
// You'll fork and manage fibers:
for {
  fiber <- task.start // Returns CancelableFuture
  _ <- Task.sleep(5.seconds)
  _ <- fiber.cancel()
} yield ()
```

**Learning Focus**:
- start vs runToFuture vs runAsync
- Cancellation semantics
- Fiber lifecycle

### 7. **MVar & Atomic - Concurrent State**
Use for:
- Task queue implementation
- Worker coordination
- Thread-safe counters

```scala
// You'll manage shared state:
for {
  mvar <- MVar[Task].empty[Task[Unit]]
  _ <- mvar.put(someTask)
  task <- mvar.take
} yield task
```

**Learning Focus**:
- MVar for coordination (take blocks until available)
- Atomic for lock-free updates
- When to use each primitive

## Recommended Tech Stack

**Core Libraries**:
- **Monix** (3.4.1) - Task, Observable, reactive streams
- **Udash** - REST API (as mentioned in your message)
- **Circe** - JSON serialization
- **ScalaTest/MUnit** - Testing
- **Typesafe Config** - Configuration

**Optional Enhancements**:
- **Doobie** - If adding database persistence
- **Redis4cats** - For distributed task queue
- **Prometheus client** - Metrics collection

**Why Not Other Effect Systems?**
- Skip Cats Effect / ZIO for now - focus on Monix since that's what you'll use at work
- Monix Task is similar to both but has unique features (Observable integration)

## Implementation Phases

### Phase 1: Basic Task + Observable (Start Here)
**Goal**: Understand Task vs Future and basic Observable usage

- Create simple Task examples (delay, map, flatMap)
- Build Observable that emits events
- Single REST endpoint that returns Task
- Run Tasks and observe execution

**Monix Focus**:
- Task creation and execution
- runToFuture vs runAsync vs runSyncUnsafe
- Basic Observable operations (map, filter)
- Understanding lazy evaluation

**Code You'll Write**:
```scala
// Simple task that "processes" something
def processItem(id: String): Task[Result] =
  Task.eval {
    println(s"Processing $id")
    Result(id, "done")
  }.delayExecution(1.second)

// Observable that emits numbers
val numbers: Observable[Int] = 
  Observable.range(0, 100)
    .mapEval(n => Task.sleep(100.millis).map(_ => n))
```

### Phase 2: Worker Pool with Concurrent Task Processing
**Goal**: Handle multiple tasks concurrently with proper resource management

- Implement task queue (MVar or ConcurrentQueue)
- Create worker pool that pulls tasks from queue
- Process N tasks in parallel
- Handle worker failures gracefully

**Monix Focus**:
- parSequenceN for bounded parallelism
- MVar for queue implementation
- Task.race for timeout/cancellation
- Error handling with onErrorRecover

**Architecture**:
```
REST API → Task Queue → Worker Pool (5 workers) → Results
                ↓
           Observable[TaskUpdate]
```

### Phase 3: Real-Time Streaming with Backpressure
**Goal**: Stream task updates to clients, handle slow consumers

- Implement SSE endpoint with Observable
- Broadcast task updates to multiple clients
- Handle backpressure (buffer, overflow strategies)
- Client connection lifecycle

**Monix Focus**:
- Observable.multicast for hot streams
- OverflowStrategy (BackPressure, DropOld, etc.)
- Observable.bufferTimed
- Resource management for streams

**Streaming Pattern**:
```scala
val taskUpdates: Observable[TaskEvent] = ???

// SSE endpoint
def streamEvents: Task[Response] =
  Task.pure {
    Response.stream(
      taskUpdates
        .map(event => ServerSentEvent(data = event.toJson))
        .asyncBoundary(OverflowStrategy.DropOld(100))
    )
  }
```

### Phase 4: Advanced Patterns & Optimization
**Goal**: Production-ready patterns

- Task cancellation via fiber management
- Retry logic with exponential backoff
- Circuit breaker pattern for failing tasks
- Metrics collection (active tasks, throughput)

**Monix Focus**:
- Task.start for background work
- Task.doOnCancel for cleanup
- Task.restartUntil for retry logic
- Atomic counters for metrics

### Phase 5: Testing & Refinement
**Goal**: Test async code properly

- TestScheduler for deterministic testing
- Mock long-running tasks
- Test backpressure scenarios
- Load testing with concurrent clients

**Monix Focus**:
- TestScheduler.tick() for time control
- Testing Observable streams
- Cancellation testing

## Key Design Decisions

1. **Task Queue Implementation**
    - MVar (blocking take/put) vs ConcurrentQueue (non-blocking)
    - Bounded vs unbounded queue
    - Priority queue support?

2. **Backpressure Strategy**
    - Drop old events vs buffer vs fail
    - Per-client buffering or global?
    - How to communicate backpressure to clients?

3. **Scheduler Configuration**
    - Separate schedulers for IO vs compute?
    - Worker pool size (CPU count? Fixed number?)
    - How to prevent thread starvation?

4. **Error Handling**
    - Retry transient failures automatically?
    - Circuit breaker for cascading failures?
    - Dead letter queue for failed tasks?

5. **State Management**
    - In-memory (fast but lost on restart)?
    - Database (persistent but slower)?
    - Eventual consistency acceptable?

## Monix vs Future: Key Differences

You'll discover these through the project:

| Aspect | Future | Monix Task |
|--------|--------|------------|
| Evaluation | Eager (starts immediately) | Lazy (starts on runAsync/runToFuture) |
| Referential Transparency | No (side effects on creation) | Yes (pure description) |
| Cancellation | Not supported | Built-in with CancelableFuture |
| Resource Safety | Manual try-finally | Bracket pattern |
| Composition | Limited error handling | Rich combinators (timeout, retry, etc.) |
| Testing | Hard (time-dependent) | Easy (TestScheduler) |

**Practical Example**:
```scala
// Future - runs immediately!
val future = Future {
  println("Side effect!")
  expensiveComputation()
}
// ^ Already running, can't control when

// Task - just a description
val task = Task {
  println("Side effect!")
  expensiveComputation()
}
// ^ Nothing happened yet

// Explicit execution
task.runToFuture // Now it runs
```

## Learning Outcomes

By completing this project, you'll understand:

**Monix-Specific**:
- ✅ Task vs Future - when and why to use each
- ✅ Observable streams and reactive programming
- ✅ Backpressure handling strategies
- ✅ Scheduler types and execution contexts
- ✅ Resource-safe programming with bracket
- ✅ Cancellation and fiber management
- ✅ Testing async code with TestScheduler

**General Scala**:
- ✅ FlatMap and monadic composition in depth
- ✅ REST API implementation
- ✅ Concurrent programming patterns
- ✅ Type-safe domain modeling with ADTs
- ✅ Error handling with Either/Task
- ✅ Testing strategies for async systems

**Production Skills**:
- ✅ Building non-blocking APIs
- ✅ Managing worker pools
- ✅ Real-time data streaming
- ✅ System observability (metrics, logging)
- ✅ Graceful shutdown and resource cleanup

## Getting Started - First Steps

1. **Setup Project**
   ```
   sbt new scala/scala-seed.g8
   ```
   Add Monix dependency to build.sbt:
   ```scala
   libraryDependencies += "io.monix" %% "monix" % "3.4.1"
   ```

2. **Experiment with Task (30 min)**
    - Create a Task that prints "Hello"
    - Chain Tasks with flatMap
    - Run with runToFuture and observe when it executes
    - Try map, flatMap, and for-comprehension

3. **Ask Claude About FlatMap (Seriously!)**
    - "Explain flatMap in Scala with Monix Task examples"
    - "Show me difference between map and flatMap with Task"
    - "What does 'flattening' mean in context of Task[Task[A]]?"

4. **Simple Observable Exercise (30 min)**
    - Create Observable that emits 1 to 10
    - Transform with map and filter
    - Subscribe and print each element
    - Try bufferTimed and see buffering in action

5. **Start Phase 1 Implementation**
    - Define domain model (Task, TaskStatus, TaskResult)
    - Create one REST endpoint
    - Return Task from endpoint
    - Make it work!

## Questions to Ask Claude/Gemini

Since Monix docs are sparse, focus your AI conversations on:

1. **Task Fundamentals**
    - "How does Task differ from Future in Monix? Show me examples."
    - "Explain Task execution model - when do side effects happen?"
    - "What is referential transparency and why does Task have it?"

2. **FlatMap Deep Dive**
    - "What problem does flatMap solve? Show me Task examples."
    - "Walk me through: Task[Task[A]] -> flatMap -> Task[A]"
    - "When to use map vs flatMap vs flatten with Task?"

3. **Observable Patterns**
    - "How to implement backpressure with Monix Observable?"
    - "Hot vs Cold Observable - practical examples"
    - "How to broadcast Observable to multiple subscribers?"

4. **Resource Management**
    - "Explain Task.bracket with examples"
    - "How to ensure cleanup happens even on errors/cancellation?"
    - "Resource type vs bracket - when to use each?"

5. **Concurrency**
    - "Compare Task.sequence vs Task.parSequence"
    - "How to cancel running Tasks?"
    - "Explain MVar and when to use it"

## Additional Resources

- **Monix Docs**: https://monix.io/ (start with Task and Observable sections)
- **Udash Docs**: https://udash.io/ (skim REST guide)
- **Scala with Cats**: https://www.scalawithcats.com/ (for understanding type classes and functors)
- **Your Coworkers**: Best resource! They'll explain company-specific patterns

## Success Criteria

You'll know you've learned Monix when you can:
- [ ] Explain why Task is lazy and when that matters
- [ ] Write code using flatMap without thinking twice
- [ ] Handle errors in Task chains properly
- [ ] Implement backpressure in Observable streams
- [ ] Cancel running operations safely
- [ ] Choose appropriate Scheduler for different work types
- [ ] Test async code with TestScheduler
- [ ] Manage resources without leaks

## Final Tips

- **Start small**: Don't build everything at once. Get Task + Simple API working first.
- **Use AI liberally**: Monix docs are thin, Claude/Gemini can explain concepts clearly.
- **Experiment in REPL**: Use `sbt console` to try Task/Observable operations interactively.
- **Read your company's code**: Once you start, real examples will clarify patterns.
- **Don't worry about perfection**: Goal is learning, not production-ready code.

Good luck! The concepts will click after you write some code. Task and flatMap might seem magical at first, but they're just patterns - patterns you'll use every day in your new role.