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

package lt.slabs.remora;

import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.NotNull;

public class MyWrappedInputStream extends InputStream {
	private final InputStream target;

	public MyWrappedInputStream(InputStream target) {
		this.target = target;
	}

	@Override
	public int read() throws IOException {
		System.out.println("Wrapper Reading");
		return target.read();
	}

	@Override
	public int read(@NotNull byte[] b) throws IOException {
		System.out.println("Wrapper Reading buffer");
		return target.read(b);
	}

	@Override
	public int read(@NotNull byte[] b, int off, int len) throws IOException {
		System.out.println("Wrapper Reading buffer " + len);
		return target.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		System.out.println(" Wrapper Closing");
		target.close();
	}
}
