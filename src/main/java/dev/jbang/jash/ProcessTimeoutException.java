/*-
 *  § 
 * jash
 *    
 * Copyright (C) 2020 OnGres, Inc.
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
 * § §
 */

package dev.jbang.jash;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;

public class ProcessTimeoutException extends ProcessException {

	private static final long serialVersionUID = 1L;

	private final Duration timeout;

	public ProcessTimeoutException(Duration timeout, Collection<String> args) {
		super(buildMessage(timeout, args), 1, args);
		this.timeout = timeout;
	}

	private static String buildMessage(Duration timeout, Collection<String> args) {
		return "Command '" + args.stream().collect(Collectors.joining(" "))
				+ "' timeout after " + timeout.toString();
	}

	public Duration getTimeout() {
		return timeout;
	}
}
