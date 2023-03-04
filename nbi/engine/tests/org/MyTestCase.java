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

package org;

import java.io.File;
import java.io.FileInputStream;
import junit.framework.TestCase;
import java.util.logging.LogManager;
import org.netbeans.installer.utils.FileUtils;

/**
 *
 * @author Danila Dugurov
 */
public class MyTestCase extends TestCase {
  
  public static final File testWD = new File("testWD");
  public static final File testOutput = new File("testOutput");
  
  protected void setUp() throws Exception {
    super.setUp();
    testWD.mkdirs();
    testOutput.mkdirs();
  }
  
  protected void tearDown() throws Exception {
    FileUtils.deleteFile(testWD, true);
    FileUtils.deleteFile(testOutput, true);
  }
}
