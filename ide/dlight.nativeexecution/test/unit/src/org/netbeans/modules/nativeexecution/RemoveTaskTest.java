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
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

/**
 *
 * @author ak119685
 */
public class RemoveTaskTest {

    public RemoveTaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of removeDirectory method, of class RemoveTask.
     */
    @Test
    public void testRemoveDirectory() {
        System.out.println("removeDirectory"); // NOI18N

        StringBuilder rmTaskError = new StringBuilder();
        boolean forceRemoveReadOnlyFile = false;
        String fileToRemove = "/path/to/the/file/to/remove"; // NOI18N
        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
//
//        NativeTask rmTask = CommonTasksSupport.getRemoveFileTask(
//                env, fileToRemove, forceRemoveReadOnlyFile, rmTaskError);
//
//        try {
//            Integer result = rmTask.invoke(false);
//            System.out.println("RESULT: " + result);
//            if (result != 0) {
//                System.out.println("ERROR: " + rmTaskError);
//            }
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        }

//        assertEquals(expResult, result);
    }
}

