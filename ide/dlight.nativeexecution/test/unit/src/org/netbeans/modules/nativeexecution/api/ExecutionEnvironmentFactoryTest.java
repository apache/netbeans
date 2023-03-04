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
package org.netbeans.modules.nativeexecution.api;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

public class ExecutionEnvironmentFactoryTest extends NativeExecutionBaseTestCase {

    public ExecutionEnvironmentFactoryTest(String name) {
        super(name);
    }

    public ExecutionEnvironmentFactoryTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(ExecutionEnvironmentFactoryTest.class);
    }

    private void doTestToFromUniqueID(ExecutionEnvironment env) {
        String id1 = ExecutionEnvironmentFactory.toUniqueID(env);
        assertNotNull("ExecutionEnvironmentFactory.toUniqueID returned null", id1);
        ExecutionEnvironment env2 = ExecutionEnvironmentFactory.fromUniqueID(id1);
        assertNotNull("ExecutionEnvironmentFactory.fromUniqueID returned null", env2);
        String id2 = ExecutionEnvironmentFactory.toUniqueID(env2);
        assertNotNull("ExecutionEnvironmentFactory.toUniqueID returned null", id2);
        assertTrue("fromUniqueID + toUniqueID resulted in non-equal IDs!", id1.equals(id2));
        assertTrue("toUniqueID + fromUniqueID resulted in non-equal objects!", env.equals(env2));
        assertTrue("equals() isn't symmetric!", env2.equals(env));
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testUniqueStringRemote() throws Exception {
        doTestToFromUniqueID(getTestExecutionEnvironment());
    }

    @org.junit.Test
    public void testUniqueStringLocal() throws Exception {
        doTestToFromUniqueID(ExecutionEnvironmentFactory.getLocal());
    }
}
