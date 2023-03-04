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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;

/**
 *
 * @author ak119685
 */
public class MacroHashMapTest extends NativeExecutionBaseTestCase {

    public MacroHashMapTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
     * Test of put method, of class MacroMap.
     */
    @Test
    public void test() {
        System.out.println("put"); // NOI18N
        MacroMap m = MacroMap.forExecEnv(ExecutionEnvironmentFactory.getLocal());

        m.put("PAtH", "/bin:$PATH:/home/$USER"); // NOI18N
        m.put("USER", "UserName"); // NOI18N
        m.put("Path", "/first:$PATH"); // NOI18N
        m.put("USER", "AnotherUser"); // NOI18N
        m.put("PaTH", "$PATH:/usr/bin"); // NOI18N

        System.out.println(m.toString());

        m = MacroMap.forExecEnv(ExecutionEnvironmentFactory.getLocal());

        m.put("PAtH", "/bin:$PATH:/home/$USER"); // NOI18N
        m.put("USER", "UserName"); // NOI18N
        m.put("Path", "/first:$PATH"); // NOI18N
        m.put("USER", "AnotherUser"); // NOI18N
        m.put("PaTH", "$PATH:/usr/bin"); // NOI18N

        System.out.println(m.toString());
    }

}
