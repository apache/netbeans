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
package org.netbeans.modules.nativeexecution.support;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;

/**
 *
 * @author Andrew
 */
public class ShellSessionTest extends NativeExecutionBaseTestCase {

    public ShellSessionTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of execute method, of class ShellSession.
     */
    @Test
    public void testExecute() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        System.out.println("execute unknown-command");
        ExitStatus result1 = ShellSession.execute(env, "unknown-command");
        System.out.println("Result1 [expected]: rc != 0, error is not empty and output is empty");
        System.out.println("Result1 [actual]  : ====== START =====");
        System.out.println(result1.toString());
        System.out.println("Result1 [actual]  : ======= END ======");

        String command = "echo out; echo error 1>&2; sh -c \"exit 100\"";
        System.out.println();
        System.out.println("execute '" + command + "'");
        ExitStatus result2 = ShellSession.execute(env, command);
        System.out.println("Result2 [expected]: rc == 100, error is 'error' and output is 'out'");
        System.out.println("Result2 [actual]  : ====== START =====");
        System.out.println(result2.toString());
        System.out.println("Result2 [actual]  : ======= END ======");

        assertNotSame(0, result1.exitCode);
        assertNotSame("It is expected that error stream is empty", "", result1.getErrorString().isEmpty());

        assertEquals(100, result2.exitCode);
        assertEquals("out", result2.getOutputString());
        assertEquals("error", result2.getErrorString());
    }
}
