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

import static dev.jbang.jash.Jash.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisabledOnOs(OS.WINDOWS)
public class ReadMeIT {

	/**
	 * Will stream `Stream.of("hello", "world")`:
	 */
	@Test
	public void testHelloWorld() {
		assertThat(Jash	.start("sh",
								"-c",
								"echo hello; echo world")
						.stream()
						.collect(Collectors.toList()))
														.containsExactly("hello", "world");
	}

	public static String escapeJavaString(String str) {
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			switch (c) {
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			default:
				if (c < 32 || c > 126) {
					sb.append(String.format("\\u%04x", (int) c));
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Same as above but using a shell-style call:
	 */
	@Test
	public void testHelloWorld$() {

		assertThat(
				$("echo hello; echo world")
											.stream()
											.collect(Collectors.toList()))
																			.containsExactly("hello", "world");
	}

	/**
	 * Same result as above but pipelining with `cat`:
	 */
	@Test
	public void testHelloWorld$Pipe() {
		Assertions.registerFormatterForType(String.class, value -> escapeJavaString(value));

		assertThat(
				$("echo hello; echo world")	.pipe("cat", "-n")
											.stream()
											.collect(Collectors.toList()))
																			.containsExactly("     1\thello",
																					"     2\tworld");
	}

	/**
	 * This will print "hello" followed by "world" but will fail when terminating
	 * the Java Stream:
	 */
	@Test
	public void testHelloWorldStartStream() {
		assertThatThrownBy(() -> {
			Jash.start(
					"sh",
					"-c",
					"echo hello; echo world; exit 79")
				.stream()
				.peek(System.out::println)
				.count();
		}).isInstanceOf(ProcessException.class);
	}

	/**
	 * Same output as the above but will fail when leaving the `try` block because
	 * the process exited with a non-zero exit code:
	 */
	@Test
	public void testHelloWorldStartStreamWithoutCloseAfterLast() {
		assertThatThrownBy(() -> {
			try (Stream<String> stream = Jash	.start(
														"sh",
														"-c",
														"echo hello; echo world; exit 79")
												.withoutCloseAfterLast()
												.stream()) {
				stream
						.peek(System.out::println)
						.count();
			}
		}).isInstanceOf(ProcessException.class);
	}

	/**
	 * Same result as above but will not fail at all as the process exit code is
	 * allowed:
	 */
	@Test
	public void testHelloWorldStartStreamWithAllowedExitCode() {
		assertThat(
				$("echo hello; echo world; exit 79")
													.withAllowedExitCode(79)
													.stream()
													.peek(System.out::println)
													.count())
																.isEqualTo(2);
	}

	/**
	 * Same result as above but will not fail as any exit code is allowed:
	 */
	@Test
	public void testHelloWorldStartStreamWithAnyExitCode() {
		assertThat(
				$("echo hello; echo world; exit 42")
													.withAnyExitCode()
													.stream()
													.peek(System.out::println)
													.count())
																.isEqualTo(2);
	}

	/**
	 * `ProcessTimeoutException` exception:
	 */
	@Test
	public void testHelloWorldStartStreamWithTimeout() {
		assertThatThrownBy(() -> {
			Jash.shell("sleep 3600")
				.withTimeout(Duration.of(1, ChronoUnit.SECONDS))
				.stream()
				.count();
		}).isInstanceOf(ProcessTimeoutException.class);
	}

}