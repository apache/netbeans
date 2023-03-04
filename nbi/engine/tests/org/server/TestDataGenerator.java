/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.*;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.exceptions.UnexpectedExceptionError;

/**
 *
 * @author Danila_Dugurov
 */
public class TestDataGenerator {
  
  public static final int K_BYTE = 1024;
  public static final int M_BYTE = 1024 * K_BYTE;
  
  File dirToGenerate;
  
  public static final String[] testFiles = new String[] {
    "smallest.data",
    "small.data",
    "smallaverage.data",
    "bigaverage.data",
    "big.data",
    "bigest.data"};
  
  public static final int[] testFileSizes = new int[] {
    K_BYTE - 435,
    M_BYTE - 237 * K_BYTE - 1,
    10 * M_BYTE - 139 * K_BYTE - 23,
    50 * M_BYTE - K_BYTE - 758,
    100 * M_BYTE - 3,
    200 * M_BYTE
  };
  
  public static final byte MAGIC_BYTE = (byte) 201;
  
  public static final byte[] buffer = new byte[4096];
  
  static {
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = MAGIC_BYTE;
    }
  }
  
  public static final URL[] testUrls = new URL[testFiles.length];
  
  static {
    try {
      for (int i = 0 ; i < testUrls.length; i++) {
        testUrls[i] = new URL("http://localhost:" + WithServerTestCase.PORT + "/" + testFiles[i]);
      }
    } catch (MalformedURLException mustNotHappend) {
      throw new UnexpectedExceptionError("wrong urls!", mustNotHappend);
    }
  }
  
  public TestDataGenerator(String dirToGenerate) {
    this.dirToGenerate = new File(dirToGenerate);
    this.dirToGenerate.mkdirs();
  }
  
  public void generateTestData() throws IOException {
    for (int index = 0; index < testFiles.length; index++) {
      final File file = new File(dirToGenerate, testFiles[index]);
      if (file.exists()) continue;
      fillWithMagicBytes(file, index);
    }
  }
  
  private void fillWithMagicBytes(File testFile, int index) throws IOException {
    OutputStream out = new BufferedOutputStream(new FileOutputStream(testFile));
    int alreadyWritten = 0;
    while (alreadyWritten < testFileSizes[index]) {
      int writeCount = alreadyWritten + buffer.length <= testFileSizes[index] ? buffer.length : testFileSizes[index] - alreadyWritten;
      out.write(buffer, 0, writeCount);
      alreadyWritten += writeCount;
    }
    out.flush();
    out.close();
  }
  
  public void deleteTestData() {
    for (int index = 0; index < testFiles.length; index++) {
      final File file = new File(dirToGenerate, testFiles[index]);
      file.delete();
    }
  }
}
