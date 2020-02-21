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

package org.netbeans.modules.remote.impl.fs.server;

/**
 *
 */
public class FsServerLocalTestCase extends FsServerLocalTestBase {

    public FsServerLocalTestCase(String name) {
        super(name);
    }
    
    public void testSecondServerFails() throws Exception {
        FSServer server1 = new FSServer("-v");
        FSServer server2 = new FSServer("-v");
        server2.requestQuit();
        int rc2 = timedWait(server1.getProcess(), 2000);
        assertNotSame("Return value of the 2-nd server should not be 0", 0, rc2);        
        server1.requestQuit();
        timedWait(server1.getProcess(), 1000);
    }
    
    public void testSimple() throws Exception {
        doSimpleTest(getWorkDir().getParentFile(), 2, "-v");
    }    
}
