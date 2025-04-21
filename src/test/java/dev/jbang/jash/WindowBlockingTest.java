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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Adding test based on
 * https://gist.github.com/rmcdouga/d060dc91f99b8d4df14ea347c90eae20 from reddit
 * thread at https://www.reddit.com/r/java/comments/1k3gp4b/comment/mob9m7g/ on
 * Java process issues on Windows
 */
public class WindowBlockingTest {

	Path generator = Path.of("src/test/resources/LoremIpsumGenerator.java");

	@Test
	public void testWindowBlockingWithJDK() throws IOException, InterruptedException {
		Process p = new ProcessBuilder("java", generator.toString()).start();
		if (p.waitFor(5, TimeUnit.SECONDS)) {
			assertThat(p.exitValue()).isEqualTo(0);
		} else {
			throw new RuntimeException("Process timed out");
		}
	}

	@Test
	public void testWindowBlockingWithJash() throws IOException, InterruptedException {
		final var j = Jash	.start("java", generator.toString())
							.withTimeout(Duration.of(5, ChronoUnit.SECONDS));
		j.join();
	}

}