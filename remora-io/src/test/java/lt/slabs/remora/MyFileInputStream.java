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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class MyFileInputStream extends FileInputStream {
	public MyFileInputStream(Path target) throws FileNotFoundException {
		super(target.toFile());
	}

	@Override
	public int read() throws IOException {
		System.out.println("Reading");
		return super.read();
	}

	@Override
	public void close() throws IOException {
		System.out.println("Closing");
		super.close();
	}
}
