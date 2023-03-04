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
package org.netbeans.jellytools.modules.debugger.actions;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperator;

/**
 * Test BreakpointsWindowAction.
 *
 * @author Martin Schovanek
 */
public class BreakpointsWindowActionTest extends JellyTestCase {

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public BreakpointsWindowActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(BreakpointsWindowActionTest.class, "testPerformMenu");
    }

    /** Test performMenu() method. */
    public void testPerformMenu() {
        new BreakpointsWindowAction().performMenu();
        new BreakpointsWindowOperator().close();
    }
}
