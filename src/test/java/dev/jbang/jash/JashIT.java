/*-
 *  § 
 * jash
 *    
 * Copyright (C) 2019 OnGres, Inc.
 *    
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * § §
 */

package dev.jbang.jash;

import static dev.jbang.jash.Jash.shell;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisabledOnOs(OS.WINDOWS)
public class JashIT {

	@Test
	public void test() throws Exception {
		assertThat(Jash	.start("sh", "-c", "echo hello; echo world")
						.stream()
						.collect(Collectors.toList()))
														.containsExactly("hello", "world");
	}

	@Test
	public void testShell() throws Exception {
		assertThat(Jash	.shell("echo hello; echo world")
						.stream()
						.collect(Collectors.toList()))
														.containsExactly("hello", "world");
	}

	@Test
	public void testShellPipe() throws Exception {
		assertThat(Jash	.$("echo hello; echo world")
						.pipe$("cat")
						.stream()
						.collect(Collectors.toList()))
														.containsExactly("hello", "world");
	}

	@Test
	public void testShellBuilder() throws Exception {
		assertThat(
				Jash.builder("echo hello; echo world")
					.withShell()
					.start()
					.pipe$("cat")
					.stream()
					.collect(Collectors.toList()))
													.containsExactly("hello", "world");
	}

	@Test
	public void testBadShellBuilder() throws Exception {
		assertThrows(RuntimeException.class, () -> { // TODO: should be more specific exception
			Jash.builder("echo hello; echo world")
				.withShell("bad-shell", "-c")
				.start()
				.get();
		});
	}

	@Test
	public void testJashToString() throws Exception {
		// todo: richer toString? i.e. running or not, command, args, etc.
		assertThat(Jash.start("java", "-version").toString())
																.contains("command=java,-version");

		assertThat(Jash.$("java -version").toString())
														.contains("sh,-c,java -version");

	}

	@Test
	public void testSuccessful() throws Exception {
		assertThat(Jash	.start("sh", "-c", "echo hello world")
						.isSuccessful())
										.isTrue();
	}

	@Test
	public void testUnsuccessful() throws Exception {
		assertThat(Jash	.start("sh", "-c", "[ -z 'hello world' ]")
						.isSuccessful())
										.isFalse();
	}

	@Test
	public void testJoin() throws Exception {
		Jash.start("sh", "-c", "echo hello world")
			.join();
	}

	@Test
	public void testPipe() throws Exception {
		assertThat(Jash	.start("sh", "-c", "echo hello; echo world")
						.pipe("cat")
						.stream()
						.collect(Collectors.toList()))
														.containsExactly("hello", "world");
	}

	@Test
	public void testPipeWithLines() throws Exception {
		assertThat(Jash	.start("sh", "-c", "echo hello world")
						.pipe("cat")
						.get())
								.isEqualTo("hello world");
	}

	@Test
	public void testEnvironment() throws Exception {
		assertThat(Jash	.builder("env")
						.clearEnvironment()
						.environment("A", "1")
						.environment("B", "2")
						.start()
						.get())
								.isEqualTo("A=1\nB=2");
	}

	@Test
	public void testInput() throws Exception {
		try (Stream<String> inputStream = Stream.of("hello world")) {
			assertThat(Jash	.start("cat")
							.inputStream(inputStream)
							.get())
									.isEqualTo("hello world");
		}
	}

	@Test
	public void testWriteToOutputStream() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Jash.start("sh", "-c", "echo hello world")
			.writeToOutputStream(outputStream);
		assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8))
																					.isEqualTo("hello world\n");
	}

	@Test
	public void testError() throws Exception {

		Assertions.assertThrows(ProcessException.class,
				() -> Jash	.start("sh", "-c", "exit 1")
							.get());
	}

	@Test
	public void testDontCloseAfterLast() throws Exception {
		Stream<String> output = Jash.builder("sh", "-c", "79")
									.dontCloseAfterLast()
									.start()
									.stream();
		assertThat(output.collect(Collectors.joining()))
														.matches(".*79.* not found");

		ProcessException ex = catchThrowableOfType(() -> {
			output.close();
			Assertions.fail();
		}, ProcessException.class);

		assertThat(ex).hasMessage("Command 'sh -c 79' exited with code 127");
		assertThat(ex.getExitCode()).isEqualTo(127);
		assertThat(ex.getArgs())
								.containsExactly("sh", "-c", "79");
	}

	@Test
	public void testTryGet() throws Exception {
		Output output = Jash.start("sh", "-c", "echo hello world")
							.tryGet();
		assertThat(output.output().get())
											.isEqualTo("hello world");
		assertThat(output.exception().isPresent())
													.isFalse();
	}

	@Test
	public void testTryGetWithError() throws Exception {
		Output output = Jash.start("sh", "-c", "echo hello world; 79")
							.tryGet();
		assertThat(output.output().isPresent())
												.isFalse();
		assertThat(output.exception().get() instanceof ProcessException)
																		.isTrue();
		ProcessException ex = (ProcessException) output.exception().get();
		assertThat(ex.getMessage())
									.isEqualTo("Command 'sh -c echo hello world; 79' exited with code 127");
		assertThat(ex.getExitCode())
									.isEqualTo(127);
		assertThat(ex.getArgs())
								.containsExactly("sh", "-c", "echo hello world; 79");
	}

	@Test
	public void testTryGetWithDontCloseAfterLast() throws Exception {
		Output output = Jash.builder("sh", "-c", "echo hello world; 79")
							.dontCloseAfterLast()
							.start()
							.tryGet();
		Assertions.assertEquals("hello world",
				output.output().get());
		Assertions.assertTrue(output.exception().get() instanceof ProcessException);
		ProcessException ex = (ProcessException) output.exception().get();
		Assertions.assertEquals("Command 'sh -c echo hello world; 79' exited with code 127",
				ex.getMessage());
		Assertions.assertEquals(127, ex.getExitCode());
		Assertions.assertIterableEquals(
				Stream	.of("sh", "-c", "echo hello world; 79")
						.collect(Collectors.toList()),
				ex.getArgs());
	}

	@Test
	public void testExitCode() throws Exception {
		try {
			Jash.start("sh", "-c", "exit 79")
				.get();
			Assertions.fail();
		} catch (ProcessException ex) {
			Assertions.assertEquals(79, ex.getExitCode());
			Assertions.assertIterableEquals(
					Stream	.of("sh", "-c", "exit 79")
							.collect(Collectors.toList()),
					ex.getArgs());
		}
	}

	@Test
	public void testAllowedExitCode() throws Exception {
		Jash.builder("sh", "-c", "exit 79")
			.allowedExitCode(79)
			.start()
			.get();
	}

	@Test
	public void testBuilderAnyExitCode() throws Exception {
		assertThat(Jash	.builder("sh", "-c", "exit 42")
						.withAnyExitCode()
						.start()
						.getExitCode())
										.isEqualTo(42);
	}

	@Test
	public void testAnyExitCode() throws Exception {
		assertThat(shell("exit 42")
									.withAnyExitCode()
									.getExitCode())
													.isEqualTo(42);
	}

	@Test
	public void testStream() throws Exception {
		assertThat(Jash	.start("sh", "-c", "echo hello world")
						.stream()
						.peek(line -> assertThat(line).isEqualTo("hello world"))
						.count())
									.isEqualTo(1);
	}

	@Test
	public void testStreamEmptyBuffer() throws Exception {
		Assertions.assertTimeout(Duration.of(1, ChronoUnit.SECONDS),
				() -> Assertions.assertEquals(1,
						Jash.start("sh", "-c", "echo hello world; sleep 2")
							.stream()
							.peek(line -> Assertions.assertEquals("hello world", line))
							.limit(1)
							.count()));
	}

	@Test
	public void testTimeout() throws Exception {
		Assertions.assertThrows(ProcessTimeoutException.class,
				() -> Jash	.start("sh", "-c", "sleep 3600")
							.withTimeout(Duration.of(10, ChronoUnit.MILLIS))
							.stream()
							.count());
	}

	@Test
	public void testTimeoutWithData() throws Exception {
		Assertions.assertTimeout(Duration.of(1, ChronoUnit.SECONDS),
				() -> Assertions.assertThrows(ProcessTimeoutException.class,
						() -> Jash	.start("sh", "-c", "sleep 3600")
									.withTimeout(Duration.of(10, ChronoUnit.MILLIS))
									.streamBytes()
									.count()));
	}

	@Test
	@Disabled("Test disabled as it hits memory limits by producing constant stream of zero bytes")
	public void testLongPipedExecutionWithTimeout() throws Exception {
		Assertions.assertTimeout(Duration.of(2, ChronoUnit.SECONDS),
				() -> Assertions.assertThrows(ProcessTimeoutException.class,
						() -> Jash	.start("cat", "/dev/zero")
									.pipe(Jash.start("cat"))
									.withTimeout(Duration.of(1, ChronoUnit.SECONDS))
									.stream()
									.count()));
	}

	@Test
	public void testInteractive() throws Exception {
		try (Stream<String> inputStream = Stream.of("hello world");
				Jash jash = Jash.start(
						"sh", "-ec", "read INPUT; echo \"$INPUT\"")
								.withoutCloseAfterLast()) {
			Assertions.assertEquals("hello world",
					jash
						.inputStreamWihtoutClosing(inputStream)
						.get());
		}
	}

	static void updateConfigurationWith(Properties propertyFile, boolean append) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			propertyFile.store(bytes, propertyFile.getProperty("test.name"));
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes.toByteArray());
			Function<String, BiFunction<String, String, String>> remapper = append
					? (x) -> ((o, n) -> n == null ? o : n)
					: (x) -> ((o, n) -> n);
			LogManager.getLogManager().updateConfiguration(bais, remapper);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
