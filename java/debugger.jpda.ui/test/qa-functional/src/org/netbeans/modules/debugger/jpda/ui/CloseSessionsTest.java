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

/**
 *
 * @author cyhelsky, jtulach, mentlicher
 */

package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;


public class CloseSessionsTest extends DebuggerTestCase {

    public CloseSessionsTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }


    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(CloseSessionsTest.class).addTest(
            "testAllSessionsClosed"
            ).enableModules(".*").clusters(".*"));
    }

    /** setUp method  */
    @Override
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    @Override
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");        
    }



    public void testAllSessionsClosed() {        
        //open source
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().perform(beanNode); // NOI18N
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        new EventTool().waitNoEvent(1000);
        //place breakpoint
        Utilities.toggleBreakpoint(eo, 104);
        //start debugging
        new DebugJavaFileAction().perform(beanNode);
        //wait for breakpoint
        Utilities.waitStatusText("Thread main stopped at MemoryView.java:104");
        List<? extends JPDADebugger> list = DebuggerManager
                .getDebuggerManager()
                .getCurrentSession()
                .lookup(null, JPDADebugger.class);
        JPDADebugger debugger = list.get(0);
        WeakReference<? extends JPDADebugger> debuggerRef = new WeakReference<>(debugger);

        //finish debugging
        Utilities.endAllSessions();
        //close sources
        eo.close();
        //nulling all temporary variables which could hold some references to debugger
        list = null;
        debugger = null;
        System.gc();
        try {
            NbTestCase.assertGC("All the debugging sessions were not correctly closed", debuggerRef);
        } catch (OutOfMemoryError u) {
            System.out.println(u.getMessage());
        }
    }
}

