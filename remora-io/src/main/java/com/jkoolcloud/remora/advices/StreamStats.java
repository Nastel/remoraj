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

package com.jkoolcloud.remora.advices;

import java.util.concurrent.atomic.AtomicLong;

public class StreamStats {
	public AtomicLong count = new AtomicLong();
	public long accessTimestamp;
	public long starttime;

	public void advanceCount() {
		count.incrementAndGet();

		accessTimestamp = System.currentTimeMillis();
	}

	public void advanceCount(int bytes) {
		count.addAndGet(bytes);

		accessTimestamp = System.currentTimeMillis();
	}
}
