/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.server;

import org.*;
//import org.server.impl.DefaultJettyServer;

/**
 *
 * @author Danila_Dugurov
 */
/**
 *this test case which invoke test server and generate test data, offcouse if wasn't generated yet.
 */
public class WithServerTestCase extends MyTestCase {
  
  public static final int PORT = 8080;
  
//  private final AbstractServer server = new DefaultJettyServer("testData", PORT);
  private final TestDataGenerator dataGenerator = new TestDataGenerator("testData");
  
  protected void setUp() throws Exception {
//    super.setUp();
//    dataGenerator.generateTestData();
//    server.start();
  }
  
  protected void tearDown() throws Exception {
//    server.stop();
//    dataGenerator.deleteTestData();
//    //this method is depricated because test data rather big
//    //and it's not good idea to delete and generate it after every test.
//    super.tearDown();
  }
}
