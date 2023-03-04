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

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OutlineOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;


public class ViewsTest extends DebuggerTestCase {

    private static String[] tests16 = new String[]{
        "testViewsDefaultOpen",
        "testViewsCallStack",
        "testViewsHeapWalker1",
        "testViewsThreads",
        "testViewsSessions",
        "testViewsSources",
        "testViewsClose"
    };

    private static String[] tests15 = new String[]{
        "testViewsDefaultOpen",
        "testViewsCallStack",
        "testViewsClasses",
        "testViewsThreads",
        "testViewsSessions",
        "testViewsSources",
        "testViewsClose"
    };

    private Node beanNode;

    public ViewsTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        String vers = System.getProperty("java.version");
        String[] tests;
        if (vers.startsWith("1.6")) 
            tests = tests16;
        else
            tests = tests15;

        return createModuleTest(ViewsTest.class, tests);
                
    }     
    
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
        if (beanNode == null) {
            beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
        }
    }
        
    public void testViewsDefaultOpen() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        new EventTool().waitNoEvent(1500);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        assertNotNull("Variables view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.variablesViewTitle, 0));
        assertNotNull("Breakpoints view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.breakpointsViewTitle, 0));
    }
    
    public void testViewsCallStack() {                        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        Utilities.showDebuggerView(Utilities.callStackViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.callStackViewTitle));
        assertEquals("MemoryView.updateStatus:92", Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()));
        assertEquals("MemoryView.updateConsumption:80", Utilities.removeTags(jTableOperator.getValueAt(1,0).toString()));
        assertEquals("MemoryView.main:117", Utilities.removeTags(jTableOperator.getValueAt(2,0).toString()));
    }
    
    public void testViewsClasses() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        Utilities.showDebuggerView(Utilities.classesViewTitle);
        OutlineOperator outlineOperator = new OutlineOperator(new TopComponentOperator(Utilities.classesViewTitle));
        new OutlineNode(outlineOperator, "Application Class Loader|examples.advanced|MemoryView|1").expand();
        String[] entries = {"System Class Loader", "Application Class Loader", "examples.advanced", "Helper", "MemoryView", "1"};
        for (int i = 0; i < entries.length; i++) {
            assertTrue("Node " + entries[i] + " not displayed in Classes view", entries[i].equals(Utilities.removeTags(outlineOperator.getValueAt(i, 0).toString())));
        }
    }
    
    public void testViewsHeapWalker1() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        Utilities.showDebuggerView(Utilities.classesViewTitle);
        TopComponentOperator tco = new TopComponentOperator(Utilities.classesViewTitle);
        JTableOperator jTableOperator = new JTableOperator(tco);
        JComboBoxOperator filter = new JComboBoxOperator(tco);
        filter.clearText();
        filter.enterText("example");
        filter.pushKey(KeyEvent.VK_ENTER);
        new EventTool().waitNoEvent(500);
        assertEquals("MemoryView class is not in classes", "examples.advanced.MemoryView", Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()));
        assertEquals("Instances number is wrong", "1 (0%)", Utilities.removeTags(jTableOperator.getValueAt(0,2).toString()));
    }
    
    public void testViewsHeapWalker2() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        Utilities.showDebuggerView(Utilities.classesViewTitle);

        TopComponentOperator tco = new TopComponentOperator(Utilities.classesViewTitle);
        JTableOperator jTableOperator = new JTableOperator(tco);
        JComboBoxOperator filter = new JComboBoxOperator(tco);
        JPopupMenuOperator popup = new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0));
        popup.pushMenuNoBlock("Show in Instances View");
        filter.clearText();
        filter.pushKey(KeyEvent.VK_ENTER);
        new EventTool().waitNoEvent(500);
    }
    
    public void testViewsThreads() {      
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        Utilities.showDebuggerView(Utilities.threadsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.threadsViewTitle));
        assertTrue("Thread group system is not shown in threads view", "system".equals(Utilities.removeTags(jTableOperator.getValueAt(0,0).toString())));
        assertTrue("Thread group main is not shown in threads view", "main".equals(Utilities.removeTags(jTableOperator.getValueAt(1,0).toString())));
        assertTrue("Thread main is not shown in threads view", "main".equals(Utilities.removeTags(jTableOperator.getValueAt(2,0).toString())));
        assertTrue("Thread Reference Handler is not shown in threads view", "Reference Handler".equals(Utilities.removeTags(jTableOperator.getValueAt(3,0).toString())));
        assertTrue("Thread Finalizer is not shown in threads view", "Finalizer".equals(Utilities.removeTags(jTableOperator.getValueAt(4,0).toString())));
        assertTrue("Thread Signal Dispatcher is not shown in threads view", "Signal Dispatcher".equals(Utilities.removeTags(jTableOperator.getValueAt(5,0).toString())));
    }
    
    public void testViewsSessions() {            
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        Utilities.showDebuggerView(Utilities.sessionsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.sessionsViewTitle));
        assertEquals("examples.advanced.MemoryView", Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()));
        try {
            org.openide.nodes.Node.Property property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(0,1);
            assertEquals("Stopped", Utilities.removeTags(property.getValue().toString()));
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(0,2);
            assertEquals("org.netbeans.api.debugger.Session localhost:examples.advanced.MemoryView", Utilities.removeTags(property.getValue().toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(ex.getClass()+": "+ex.getMessage(), false);
        }
    }
    
    public void testViewsSources() {        
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        Utilities.showDebuggerView(Utilities.sourcesViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.sourcesViewTitle));
        String debugAppSource = "debugTestProject" + java.io.File.separator + "src (Project debugTestProject)";
        boolean jdk = false, project = false;
        for (int i=0;i < jTableOperator.getRowCount();i++) {
            String src = Utilities.removeTags(jTableOperator.getValueAt(i,0).toString());
            if (src.endsWith("src.zip")) {
                jdk=true;
            } else if (src.endsWith(debugAppSource)) {
                project = true;
            }
        }
        assertTrue("JDK source root is not shown in threads view", jdk);
        assertTrue("MemoryView source root is not shown in threads view", project);
    }
    
    public void testViewsClose() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 92);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:92");
        new TopComponentOperator(Utilities.variablesViewTitle).close();
        //new TopComponentOperator(Utilities.watchesViewTitle).close();
        new TopComponentOperator(Utilities.callStackViewTitle).close();
        new TopComponentOperator(Utilities.classesViewTitle).close();
        new TopComponentOperator(Utilities.sessionsViewTitle).close();
        new TopComponentOperator(Utilities.threadsViewTitle).close();
        new TopComponentOperator(Utilities.sourcesViewTitle).close();
    }
}
