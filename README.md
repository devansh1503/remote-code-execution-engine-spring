# Remote Code Execution Engine

A backend service that safely runs user-submitted code in isolated environments and returns the result. Think of it as the engine behind platforms like LeetCode or HackerRank — but simplified and built for learning, experimentation, and extension.

## What this project does
	1.	You send some code (Python, Java, JavaScript, or C++) to an API.
	2.	The system puts that request in a queue.
	3.	A worker picks it up and runs the code inside a temporary Docker container.
	4.	The output, errors, and execution time are captured.
	5.	You can later ask for the result using a job ID.

Each run is isolated, short-lived, and cleaned up immediately after execution.

## Why this exists

This project was built to understand how real-world online code judges work:
	•	How to safely execute untrusted code
	•	How to scale execution using queues and workers
	•	How to keep executions isolated and reproducible

It is intentionally backend-focused and API-driven.


## High-level architecture
```
Client
  ↓
REST API (Spring Boot)
  ↓
Redis Queue  ←── Job Metadata
  ↓
Worker (Scheduler)
  ↓
Docker Container (per execution)
  ↓
Result stored back in Redis


You submit code → get a job ID → poll for results.
```


## Supported languages
	•	Python
	•	JavaScript (Node.js)
	•	Java
	•	C++

Each language runs in its own official Docker image.


## Key features
	•	Isolated execution – Every request runs in a fresh container
	•	Queue-based processing – Prevents overload and enables scaling
	•	Execution time tracking
	•	STDOUT + STDERR capture
	•	Language-agnostic API
	•	Problem management – Store coding problems with difficulty levels
	•	AI-based complexity verification – Validate time/space complexity using LLMs


## API overview (simplified)

Submit code for execution
```
POST /api/execute

Request:

{
  "code": "print('Hello World')",
  "language": "python",
  "stdin": ""
}

Response:

{
  "id": "job-id",
  "status": "SUBMITTED"
}
```


⸻

## Get execution result

```
GET /api/result/{jobId}

Response:

{
  "status": "COMPLETED",
  "result": {
    "output": "Hello World",
    "executionTime": "0.012 Seconds"
  }
}
```



## Problems & difficulty management
	•	Create coding problems
	•	Filter by difficulty (EASY / MEDIUM / HARD)
	•	Retrieve problem statements and solutions

This makes the engine usable as a full coding practice backend.

## How execution actually works
	•	A worker runs every few seconds
	•	It pulls the next job ID from Redis
	•	Spins up a language-specific Docker container
	•	Injects the code and input
	•	Executes it with a timeout
	•	Captures output and execution time
	•	Destroys the container immediately

No container is reused. No state leaks.


## Running locally (recommended way)

Prerequisites
	•	Docker + Docker Compose
	•	Java 17

Start everything

docker-compose up --build

This will start:
	•	Spring Boot API (port 8080)
	•	MySQL
	•	Redis

Docker socket is mounted so the app can create containers dynamically.


## Environment variables
	•	OPENAI_API_KEY – Required only for complexity analysis feature


## Security notes

This project is for learning and controlled environments.

Before using in production, you would need:
	•	Resource limits (CPU / memory)
	•	Stricter timeouts
	•	Network isolation
	•	Image hardening
	•	Abuse prevention


## Future improvements
	•	Multiple workers for parallel execution
	•	Language sandboxing with stricter limits
	•	Job prioritization
	•	Persistent execution logs
	•	WebSocket-based live output


## Who should look at this project
	•	Backend engineers
	•	Systems / platform engineers
	•	Anyone curious how online judges work
	•	Interview preparation for distributed systems

⸻

## Author

Built by Devansh as a deep dive into remote execution systems.
