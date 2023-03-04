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
package org.netbeans.modules.testng.ui.actions;

import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.netbeans.modules.testng.ui.impl.TestNGImpl;
import org.netbeans.spi.project.SingleMethod;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeActionsInfraHid;
import org.openide.util.actions.SystemAction;
import org.testng.annotations.Test;

/**
 *
 * @author lukas
 */
@Test(enabled=false)
public class RunTestMethodActionTest extends TestActionT {

    static {
        TestNGImpl.setSupportedActions(Action.RUN_TESTMETHOD);
    }
    private final RunTestMethodAction action = SystemAction.get(RunTestMethodAction.class);

    public RunTestMethodActionTest(String name) {
        super(name);
    }

    public void testEnable() {
        NodeActionsInfraHid.setCurrentNodes(EMPTY_ARRAY);
        assertFalse(action.isEnabled());
        NodeActionsInfraHid.setCurrentNodes(EMPTY_NODES);
        assertFalse(action.isEnabled());
        NodeActionsInfraHid.setCurrentNodes(DATAOBJECT_NODE);
        assertTrue(action.isEnabled());
        NodeActionsInfraHid.setCurrentNodes(new Node[]{
                    new NodeImpl(new SingleMethod(p.getProjectDirectory(), "myMethod"))});
        assertTrue(action.isEnabled());
    }
}