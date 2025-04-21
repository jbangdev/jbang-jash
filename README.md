# jash

![Maven Central Version](https://img.shields.io/maven-central/v/dev.jbang/jash)


## Table of content

* [What is it?](#what-is-it)
* [How to use it](#how-to-use-it)
* [How to build](#how-to-build)
* [Origin story](#origin-story)

## What is it?

![Jash Logo](images/jash_logo.png)

```java
import static dev.jbang.jash.Jash.*;

$("java -version").stream().forEach(System.out::println);
```

A Java library to provide a Process interface that is fluent, predictable and with a great developer experience.

*fluent* - because it provides a fluent API to start and manage processes

*predictable* - because it provides a predictable API to start and manage processes, incl. throwing an exception when the process exits with a non-zero/not-allowed exit code.

*great developer experience* - because it provides a great developer experience to work with processes, incl. streaming collecting it as a string or a list of strings.

## How to use it

Add the ![Maven Central Version](https://img.shields.io/maven-central/v/dev.jbang/jash) dependency to your project:

<details>
<summary>JBang</summary>

```java
//DEPS dev.jbang:jash:RELEASE
```
</details>

<details>
<summary>Maven</summary>

```xml
<dependency>
	<groupId>dev.jbang</groupId>
	<artifactId>jash</artifactId>
	<version>RELEASE</version>
</dependency>
```
</details>

<details>
<summary>Gradle</summary>

```groovy
dependencies {
	implementation 'dev.jbang:jash:RELEASE'
}
```
</details>


Then use one of `Jash.start(String command, String...args)` or `Jash.builder(String command, String args...args)` for a more fine grained configuration:

* Will stream `Stream.of("hello", "world")`:

```java
Jash.start(
	"sh",
	"-c",
	"echo hello; echo world")
		.stream()
```

* Same as above but using a shell-style call:

```java
import static io.ongres.jash.Jash.*;

$("echo hello; echo world").stream()
```

* As above but pipelining with `cat -n` to add line numbers:

```java
import static io.ongres.jash.Jash.*;
$("echo hello; echo world").pipe("cat", "-n").stream()
```

* Same result as above but passing to `cat` a pure Java Stream:

```java
Jash.start("cat")
		.inputStream(
		Stream.of("hello", "world"))
		.stream()
```

* This will print "hello" followed by "world" but will fail when terminating the Java Stream because the process will exit with a non-zero exit code:

```java
Jash.start(
	"sh",
	"-c",
	"echo hello; echo world; exit 79")
		.stream()
		.peek(System.out::println)
		.count() // <- exception will be thrown here
```

* Same output as the above but will fail when leaving the `try` block because the process exited with a non-zero exit code:

```java
try (Stream<String> stream = Jash.start(
	"sh",
	"-c",
	"echo hello; echo world; exit 79")
		.withoutCloseAfterLast()
		.stream()) {
	stream
		.peek(System.out::println)
		.count();
} // <- exception will be thrown here when Stream.close() will be called
```

* Same result as above but will not fail at all as the process exit code is allowed:

```java
Jash.$("echo hello; echo world; exit 79")
		.withAllowedExitCode(79)
		.stream()
		.peek(System.out::println)
		.count();
```

* Same result as above but will not fail as any exit code is allowed:

```java
Jash.$("echo hello; echo world; exit 79")
		.withAllowedExitCode(79)
		.withAnyExitCode()
		.stream()
		.peek(System.out::println)
		.count();
```

* You can also specify a timeout that will result in a `ProcessTimeoutException` exception:

```java
Jash.shell(
	"sleep 3600")
		.withTimeout(Duration.of(1, ChronoUnit.SECONDS))
		.stream()
		.count(); // <- will throw an ProcessTimeoutException exception
```

## How to build

Java 8+ and Maven are required to build this project.

Run following command:

```bash
gradle build
```

### Integration tests

The integration test suite requires a Unix compatible system is installed on the system and and
some very common commands (sh, cat, env and sed).
To launch the integrations tests run the following command:

```bash
gradle test
```

## Origin story

This project was originally created as "fluent-process" by OnGres, Inc. in 2020. In 2025, it was renamed to "jash" (pronounced Jazz, a reference to Java and Shell) to reflect its focus on providing a more idiomatic Java 17+ interface for working with shell processes and streams.
