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

package lt.slabs.com.jkoolcloud.remora;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.io.Files;
import com.jkoolcloud.remora.Remora;
import com.jkoolcloud.remora.core.output.SysOutOutput;

/**
 * This class intended to test loading premain and it's options.
 */
public class RemoraTest2 {
	public static void main(String[] args) throws Throwable {
		System.setProperty("probe.output", SysOutOutput.class.getName());
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println( //
				"\n\tClass.loader=" + Remora.class.getClassLoader() //
						+ "\n\tJava.version=" + System.getProperty("java.version") //
						+ "\n\tJava.vendor=" + System.getProperty("java.vendor") //
						+ "\n\tJava.home=" + System.getProperty("java.home") //
						+ "\n\tJava.heap=" + Runtime.getRuntime().maxMemory() //
						+ "\n\tOS.name=" + System.getProperty("os.name") //
						+ "\n\tOS.version=" + System.getProperty("os.version") //
						+ "\n\tOS.arch=" + System.getProperty("os.arch") //
						+ "\n\tOS.cpus=" + Runtime.getRuntime().availableProcessors());

		JustATest2 justATest2 = new JustATest2();
		justATest2.onEnterAnnotatedMethod("c");

		Thread.sleep(3000);
	}

	@Test
	public void findJarsTest() throws IOException {
		File tempDir = Files.createTempDir();
		java.nio.file.Files.createTempFile(Paths.get(tempDir.getAbsolutePath()), "temp1", ".jar");
		java.nio.file.Files.createTempFile(Paths.get(tempDir.getAbsolutePath()), "temp2", ".jar");

		URL[] jars = Remora.findJars(tempDir.getAbsolutePath());
		assertEquals(2, jars.length);

		org.apache.commons.io.FileUtils.deleteDirectory(tempDir);

	}

}
