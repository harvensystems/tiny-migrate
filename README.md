# tiny-migrate

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Reactive-brightgreen)](https://spring.io/projects/spring-boot)
[![Reactive Streams](https://img.shields.io/badge/Reactive-Project%20Reactor-purple)](https://projectreactor.io/)

A lightweight, non-blocking, distributed database migration library designed specifically for **Spring Boot Reactive** applications using **MongoDB** (`ReactiveMongoTemplate`).

---

## 📌 Table of Contents

- [What is tiny-migrate?](#-what-is-tiny-migrate)
- [Why tiny-migrate?](#-why-tiny-migrate)
- [Key Features](#-key-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [How to Use](#-how-to-use)
  - [1. Define Migration Steps](#1-define-migration-steps)
  - [2. Multiple Migration Example](#2-multiple-migration-example)
  - [3. Automatic Execution](#3-automatic-execution)
- [How It Works Under the Hood](#-how-it-works-under-the-hood)
  - [Distributed Locking](#distributed-locking)
  - [Execution Log & State](#execution-log--state)
  - [Ordering](#ordering)
- [Extensibility](#-extensibility)
- [Contributing](#-contributing)
- [License](#-license)

---

## 💡 What is tiny-migrate?

**`tiny-migrate`** is a minimalist, developer-friendly migration framework for reactive Java/Spring microservices. It allows you to declare database migrations as standard Spring beans returning reactive publishers (`Mono<Void>`), ensuring that all schema alterations, index creations, data conversions, or seed tasks run safely, reactively, and idempotently during application startup.

---

## 🎯 Why tiny-migrate?

Traditional database migration tools (such as Liquibase, Flyway, or legacy MongoDB migration libraries) are typically designed for imperative, blocking execution and relational databases. When migrating reactive MongoDB applications, developers often encounter:

1. **Blocking Driver Conflicts**: Many existing tools force synchronous MongoDB drivers onto asynchronous reactive microservice stacks.
2. **Heavy Dependencies & Overhead**: Complex XML/YAML/JSON configuration overhead when all you need is programmatic reactive code execution.
3. **Clustering & Concurrency Risks**: Multiple service replicas starting at the same time can run conflicting migrations without distributed synchronization.

**`tiny-migrate`** solves these problems by providing:
- **100% Non-Blocking & Reactive Native**: Built on top of Project Reactor (`Mono`) and Spring Data MongoDB Reactive (`ReactiveMongoTemplate`).
- **Built-In Distributed Locking**: Automatically coordinates multiple application instances via atomic MongoDB lock entries with retry mechanisms.
- **Spring Boot Simplicity**: Zero configuration required — define your beans and Spring Boot handles the rest.

---

## ✨ Key Features

- ⚡ **Pure Reactive Execution**: Asynchronous non-blocking lifecycle integrating seamlessly with Spring WebFlux.
- 🔒 **Distributed Lock Management**: Guarantees that only one cluster node executes migrations at any given time.
- 📋 **Automatic Discovery**: Automatically picks up all Spring beans implementing `MigrationStep`.
- 🔢 **Deterministic Ordering**: Steps are sorted and executed sequentially by their unique identifier (`getId()`).
- 🛡️ **Idempotent & Fault-Tolerant**: Executed migrations are tracked in MongoDB; failed migrations abort the pipeline and record failure logs for quick diagnostics.
- 🔌 **Pluggable Architecture**: Easily substitute storage or lock managers with custom implementations if needed.

---

## 📋 Requirements

- **Java**: 17 or higher
- **Spring Boot**: 3.x / 4.x (Reactive Web / WebFlux)
- **Spring Data MongoDB Reactive**
- **MongoDB**: 4.x or higher

---

## 📦 Installation

Add `tiny-migrate` to your project dependencies:

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("org.harvensystems:tiny-migrate:1.0.0")
}
```

### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'org.harvensystems:tiny-migrate:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>org.harvensystems</groupId>
    <artifactId>tiny-migrate</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🚀 How to Use

### 1. Define Migration Steps

Implement the `MigrationStep` interface and register your migration as a Spring component or `@Bean`:

```java
package com.example.app.migrations;

import org.harvensystems.tinymigrate.api.MigrationStep;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class V001_CreateUserIndexesMigration implements MigrationStep {

    private final ReactiveMongoTemplate mongoTemplate;

    public V001_CreateUserIndexesMigration(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public String getId() {
        return "20261012_01_create_user_email_index";
    }

    @Override
    public Mono<Void> run() {
        return mongoTemplate.indexOps("users")
                .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique())
                .then();
    }
}
```

### 2. Multiple Migration Example

You can organize your migrations cleanly across classes or as beans inside a `@Configuration`:

```java
package com.example.app.migrations;

import org.harvensystems.tinymigrate.api.MigrationStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

@Configuration
public class DatabaseMigrationsConfiguration {

    @Bean
    public MigrationStep updateAccountStatus(ReactiveMongoTemplate mongoTemplate) {
        return new MigrationStep() {
            @Override
            public String getId() {
                return "20261012_02_set_default_account_status";
            }

            @Override
            public Mono<Void> run() {
                Query query = Query.query(Criteria.where("status").is(null));
                Update update = Update.update("status", "ACTIVE");
                return mongoTemplate.updateMulti(query, update, "accounts")
                        .then();
            }
        };
    }
}
```

### 3. Automatic Execution

Once your application starts up:
1. `TinyMigrateReactiveAutoConfiguration` activates when `ReactiveMongoTemplate` is on the classpath.
2. `ReactiveMigrationRunner` acquires the distributed lock (`migrations_lock`).
3. It fetches all `MigrationStep` beans, orders them by `getId()`, checks if each step was previously executed successfully in the `migrations` collection, and executes missing steps sequentially.
4. It logs the result (`SUCCESS` or `FAILURE`) and safely releases the lock.

---

## ⚙️ How It Works Under the Hood

### Distributed Locking
When running multiple instances of your microservice (e.g. in Kubernetes), only one instance should perform database migrations.
- `tiny-migrate` attempts an atomic insert into the `migrations_lock` collection with `_id: "migration"`.
- If another instance holds the lock, subsequent nodes catch `DuplicateKeyException` and automatically retry periodically (e.g., every 5 seconds up to 20 times).
- Once migrations conclude (or fail), the lock is released cleanly.

### Execution Log & State
Migrations are recorded in the `migrations` collection:
```json
{
  "_id": "20261012_01_create_user_email_index",
  "createdAt": "2026-10-12T12:00:00Z",
  "state": "SUCCESS"
}
```
- If a migration finishes without error, state is marked as `SUCCESS`.
- If a step throws an error, state is logged as `FAILURE`, the migration pipeline terminates immediately, and the exception is propagated to prevent the application from starting in an inconsistent state.

### Ordering
Migrations are executed in ascending order based on `MigrationStep#getId()`. It is recommended to prefix IDs with a chronological timestamp or version string (e.g. `V001_...`, `20261012_01_...`).

---

## 🧩 Extensibility

If you want to provide your own custom lock management or migration log persistence (e.g., Redis locking or an alternative store), implement the respective interfaces and register them as Spring beans:

- `ReactiveMigrationStorage`: Handles querying and persisting migration status.
- `ReactiveDistributedLockManager`: Handles acquiring and releasing distributed locks.

```java
@Bean
public ReactiveDistributedLockManager customLockManager() {
    return new MyCustomDistributedLockManager();
}
```

---

## 🤝 Contributing

Contributions, bug reports, and feature requests are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.