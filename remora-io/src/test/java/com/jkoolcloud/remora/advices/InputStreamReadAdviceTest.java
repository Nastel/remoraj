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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import lt.slabs.remora.MyFileInputStream;

//Enable power mockito if any of classes failing to mock
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WebApp.class})
//@SuppressStaticInitializationFor({""})
public class InputStreamReadAdviceTest {

	@Test
	public void testInputStreamInterceptor() throws NoSuchMethodException {

	}

	public static void main(String[] args) throws IOException {
		System.setProperty("remora.output", "com.jkoolcloud.remora.core.output.SysOutOutput");
		File tempFile = File.createTempFile("test", "test");
		FileWriter fileWriter = new FileWriter(tempFile);
		for (int i = 0; i < 1000; i++) {
			fileWriter.append("Line\n");
		}
		fileWriter.flush();
		fileWriter.close();
		Path target = Paths.get(tempFile.getAbsolutePath() + "copy");
		Files.copy(tempFile.toPath(), target);
		InputStream targetStream = new MyFileInputStream(target);
		targetStream.read();
		targetStream.close();
	}

}