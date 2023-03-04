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
package org.netbeans;

import org.fakepkg.FakeHandler;
import org.netbeans.junit.NbTestCase;

/** Tests that handler can set netbeans.mainclass property in its constructor.
 *
 * @author Jaroslav Tulach
 */
public class CLICanInfluenceMainClassTest extends NbTestCase {
    private static boolean called;

    public CLICanInfluenceMainClassTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public static void main(String[] args) {
        called = true;
    }

    public void testCLIChangesMainClass() throws Exception {
        class R implements Runnable {
            public void run() {
                System.setProperty("netbeans.mainclass", CLICanInfluenceMainClassTest.class.getName());
            }
        }
        
        FakeHandler.toRun = new R();
        
        org.netbeans.MainImpl.main(new String[] { "--userdir", getWorkDirPath() });
        
        assertTrue("Our main method has been called", called);
    }
}
