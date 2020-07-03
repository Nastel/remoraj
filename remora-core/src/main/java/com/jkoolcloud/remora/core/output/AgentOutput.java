/*
 * Copyright 2019-2020 NASTEL TECHNOLOGIES, INC.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jkoolcloud.remora.core.output;

import java.util.concurrent.ThreadFactory;

public interface AgentOutput<T> {
	void init() throws OutputException;

	void send(T entry);

	void shutdown();

	ThreadFactory getThreadFactory();

	class OutputException extends Exception {
		private static final long serialVersionUID = -6937653706786664128L;

		public OutputException(String message) {
			super(message);
		}
	}

}
