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
package org.netbeans.jellytools.actions;

import static org.netbeans.jellytools.actions.SaveActionTest.eo;

/**
 * Test of org.netbeans.jellytools.actions.SaveAllAction.
 *
 * @author Jiri Skrivanek
 */
public class SaveAllActionTest extends SaveActionTest {

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as test case
     */
    public SaveAllActionTest(String testName) {
        super(testName);
    }

    /**
     * method used for explicit test suite definition
     */
    public static junit.framework.Test suite() {
        return createModuleTest(SaveAllActionTest.class, "testPerformMenu", "testPerformAPI");
    }

    /**
     * Test performMenu method.
     */
    @Override
    public void testPerformMenu() {
        new SaveAllAction().performMenu();
        eo.waitModified(false);
    }

    /**
     * Test performAPI method.
     */
    @Override
    public void testPerformAPI() {
        new SaveAllAction().performAPI();
        eo.waitModified(false);
    }
}
