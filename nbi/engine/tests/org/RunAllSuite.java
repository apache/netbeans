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

package org;

import junit.framework.TestSuite;
import org.connector.ConnectionConfiguratorTest;
import org.connector.ConnectorTest_;
import org.connector.ProxySelectorTest;
import org.connector.ProxyTest;
import org.dispatcher.DispatcherTest;
import org.util.DomVisitorTest;

/**
 *
 * @author Danila_Dugurov
 */
public class RunAllSuite extends TestSuite {
    
    public RunAllSuite() {
        addTestSuite(ProxyTest.class);
        addTestSuite(ProxySelectorTest.class);
        addTestSuite(ConnectionConfiguratorTest.class);
        addTestSuite(ConnectorTest_.class);
        addTestSuite(DispatcherTest.class);
        addTestSuite(DomVisitorTest.class);
        //addTestSuite(WindowsRegistryTest.class);
        //todo: dinamic add test case without manual registration
    }
    //this done only becouse without it netbeans faild to run tests
      public void testFake() {}
}
