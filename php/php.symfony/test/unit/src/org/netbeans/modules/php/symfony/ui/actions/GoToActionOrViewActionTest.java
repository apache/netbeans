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

package org.netbeans.modules.php.symfony.ui.actions;

import org.netbeans.junit.NbTestCase;

public class GoToActionOrViewActionTest extends NbTestCase {

    public GoToActionOrViewActionTest(String name) {
        super(name);
    }

    public void testGetActionMethodName() {
        assertEquals("executeIndex", SymfonyGoToActionAction.getActionMethodName("indexSuccess"));
        assertEquals("executeNew", SymfonyGoToActionAction.getActionMethodName("newSuccess"));
        assertEquals("executeNew", SymfonyGoToActionAction.getActionMethodName("newError"));
        assertEquals("executeIndexDemo", SymfonyGoToActionAction.getActionMethodName("indexDemoSuccess"));
        assertEquals("executeIndexDemoUglyBug", SymfonyGoToActionAction.getActionMethodName("indexDemoUglyBugSuccess"));
        assertEquals("executeIndex2Demo3", SymfonyGoToActionAction.getActionMethodName("index2Demo3Success"));
        assertEquals("executeIndex_Demo", SymfonyGoToActionAction.getActionMethodName("index_DemoSuccess"));

        assertEquals("execute_admin", SymfonyGoToActionAction.getActionMethodName("_adminInc"));
        assertNull(SymfonyGoToActionAction.getActionMethodName("_admin"));
    }
}
