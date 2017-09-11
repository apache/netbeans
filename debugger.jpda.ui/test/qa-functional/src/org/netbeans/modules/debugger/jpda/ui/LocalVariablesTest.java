/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 */

package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OutlineOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 *
 * @author ehucka, Jiri Vagner, cyhelsky, Jiri Kovalsky
 */
public class LocalVariablesTest extends DebuggerTestCase {
    
    private static String[] tests = new String[]{
        "testLocalVariablesThisNode",
        "testLocalVariablesStaticNode",
        "testLocalVariablesStaticInherited",
        "testLocalVariablesInheritedNode",
        "testLocalVariablesExtended",
        "testLocalVariablesValues",
        "testLocalVariablesSubExpressions"
    };

    public final String version;

    private Node projectNode;
    private Node beanNode;
    
    /**
     *
     * @param name
     */
    public LocalVariablesTest(String name) {
       super(name);
        version  = getJDKVersionCode();
    }
    
    private String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");
        
        if (specVersion.startsWith("1.4"))
            return "jdk14";
        
        if (specVersion.startsWith("1.5"))
            return "jdk15";
        
        if (specVersion.startsWith("1.6"))
            return "jdk16";
        
        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
    }
        
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    /**
     *
     * @return
     */
    public static Test suite() {
        return createModuleTest(LocalVariablesTest.class, tests);
    }
    
    /**
     *
     */
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
        if (projectNode == null) {
            projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
        }
    }
        
    /**
     *
     */
    protected void expandNodes() {
        Utilities.showDebuggerView(Utilities.variablesViewTitle);
        
        OutlineOperator lrOutlineOp = new OutlineOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        
        OutlineNode lrThisNode = lrOutlineOp.getRootNode("this");
        lrThisNode.expand();
        //Utilities.sleep(500);
        new OutlineNode(lrThisNode, "Static").expand();
        //Utilities.sleep(500);
        new OutlineNode(lrThisNode, "Inherited").expand();
        //Utilities.sleep(500);
    }
    
    /**
     *
     */
    public void testLocalVariablesThisNode() throws Throwable {        

        EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
        new EventTool().waitNoEvent(500);
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        Utilities.toggleBreakpoint(eo, 52);
        new EventTool().waitNoEvent(500);
        try {
            Utilities.startDebugger();
        } catch (Throwable th) {
            new DebugProjectAction().perform(projectNode);
        }
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.");
        expandNodes();
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        checkOutlineTableLine(outlineOp, 3, "Vpublic", "String", "\"Public Variable\"");
        checkOutlineTableLine(outlineOp, 4, "Vprotected", "String", "\"Protected Variable\"");
        checkOutlineTableLine(outlineOp, 5, "Vprivate", "String", "\"Private Variable\"");
        checkOutlineTableLine(outlineOp, 6, "VpackagePrivate", "String", "\"Package-private Variable\"");
    }
    
    /**
     *
     */
    public void testLocalVariablesStaticNode() throws Throwable {
        EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 52);
        new EventTool().waitNoEvent(500);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.");
        expandNodes();
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        checkOutlineTableLine(outlineOp, 11, "Spublic", "String", "\"Public Variable\"");
        checkOutlineTableLine(outlineOp, 12, "Sprotected", "String", "\"Protected Variable\"");
        checkOutlineTableLine(outlineOp, 13, "Sprivate", "String", "\"Private Variable\"");
        checkOutlineTableLine(outlineOp, 14, "SpackagePrivate", "String", "\"Package-private Variable\"");
    }
    
    /**
     *
     */
    public void testLocalVariablesStaticInherited() throws Throwable {
        EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
        new EventTool().waitNoEvent(1500);
        Utilities.toggleBreakpoint(eo, 52);
        new EventTool().waitNoEvent(1500);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.");
        expandNodes();
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        checkOutlineTableLine(outlineOp, 15, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        checkOutlineTableLine(outlineOp, 16, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        checkOutlineTableLine(outlineOp, 17, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        checkOutlineTableLine(outlineOp, 18, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
    }
    
    /**
     *
     */
    public void testLocalVariablesInheritedNode() throws Throwable {
        EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 52);
        new EventTool().waitNoEvent(500);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.");
        expandNodes();
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        checkOutlineTableLine(outlineOp, 20, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        checkOutlineTableLine(outlineOp, 21, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        checkOutlineTableLine(outlineOp, 22, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        checkOutlineTableLine(outlineOp, 23, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
    }
    
    /**
     *
     */
    public void testLocalVariablesExtended() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 76);
        new EventTool().waitNoEvent(500);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76.");
        expandNodes();
        Utilities.showDebuggerView(Utilities.variablesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.variablesViewTitle));

        int count = 1;
        checkOutlineTableLine(outlineOp, count++, "this", "MemoryView", null);
        checkOutlineTableLine(outlineOp, count++, "timer", null, "null");
        checkOutlineTableLine(outlineOp, count++, "Vpublic", "String", "\"Public Variable\"");
        checkOutlineTableLine(outlineOp, count++, "Vprotected", "String", "\"Protected Variable\"");
        checkOutlineTableLine(outlineOp, count++, "Vprivate", "String", "\"Private Variable\"");
        checkOutlineTableLine(outlineOp, count++, "VpackagePrivate", "String", "\"Package-private Variable\"");
        checkOutlineTableLine(outlineOp, count++, "Static", null, null);
        checkOutlineTableLine(outlineOp, count++, "bundle", "PropertyResourceBundle", null);
        assertTrue("Node bundle has no child nodes", hasChildNodes("this|Static|bundle", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "msgMemory", "MessageFormat", null);
        assertTrue("Node msgMemory has no child nodes", hasChildNodes("this|Static|msgMemory", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "UPDATE_TIME", "int", "1000");
        checkOutlineTableLine(outlineOp, count++, "Spublic", "String", "\"Public Variable\"");
        checkOutlineTableLine(outlineOp, count++, "Sprotected", "String", "\"Protected Variable\"");
        checkOutlineTableLine(outlineOp, count++, "Sprivate", "String", "\"Private Variable\"");
        checkOutlineTableLine(outlineOp, count++, "SpackagePrivate", "String", "\"Package-private Variable\"");
        //checkOutlineTableLine(outlineOp, count++, "class$java$lang$Runtime", "Class", "class java.lang.Runtime");
        //assertTrue("Node class$java$lang$Runtime has no child nodes", hasChildNodes("this|Static|class$java$lang$Runtime", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        checkOutlineTableLine(outlineOp, count++, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        checkOutlineTableLine(outlineOp, count++, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        checkOutlineTableLine(outlineOp, count++, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        checkOutlineTableLine(outlineOp, count++, "Inherited", null, null);
        checkOutlineTableLine(outlineOp, count++, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        checkOutlineTableLine(outlineOp, count++, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        checkOutlineTableLine(outlineOp, count++, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        checkOutlineTableLine(outlineOp, count++, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        checkOutlineTableLine(outlineOp, count++, "clazz", "Class", "class java.lang.Runtime");
        assertTrue("Node clazz has no child nodes", hasChildNodes("clazz", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "string", "String", "\"Hi!\"");
        checkOutlineTableLine(outlineOp, count++, "n", "int", "50");
        checkOutlineTableLine(outlineOp, count++, "llist", "LinkedList", null);
        assertTrue("Node llist has no child nodes", hasChildNodes("llist", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "alist", "ArrayList", null);
        assertTrue("Node alist has no child nodes", hasChildNodes("alist", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "vec", "Vector", null);
        assertTrue("Node vec has no child nodes", hasChildNodes("vec", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "hmap", "HashMap", null);
        assertTrue("Node hmap has no child nodes", hasChildNodes("hmap", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "htab", "Hashtable", null);
        assertTrue("Node htab has no child nodes", hasChildNodes("htab", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "tmap", "TreeMap", null);
        assertTrue("Node tmap has no child nodes", hasChildNodes("tmap", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "hset", "HashSet", null);
        assertTrue("Node hset has no child nodes", hasChildNodes("hset", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "tset", "TreeSet", null);
        assertTrue("Node tset has no child nodes", hasChildNodes("tset", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "policko", "int[]", null);
        assertTrue("Node policko has no child nodes", hasChildNodes("policko", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "pole", "int[]", null);
        assertTrue("Node pole has no child nodes", hasChildNodes("pole", outlineOp));
        checkOutlineTableLine(outlineOp, count++, "d2", "int[][]", null);
        assertTrue("Node d2 has no child nodes", hasChildNodes("d2", outlineOp));
    }
    
    /**
     *
     */
    public void testLocalVariablesValues() throws Throwable {        
        EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 104);
        new EventTool().waitNoEvent(500);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104.");
        expandNodes();
        Utilities.showDebuggerView(Utilities.variablesViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        try {
            org.openide.nodes.Node.Property property;
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(25, 2);
            long free = Long.parseLong(property.getValue().toString());
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(26, 2);
            long total = Long.parseLong(property.getValue().toString());
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(27, 2);
            long taken = Long.parseLong(property.getValue().toString());
            assertTrue("Local varaibles values does not seem to be correct (total != free + taken) - "+total+" != "+free+" + "+taken, (total == free + taken));

        } catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
    
    /**
     *
     */
    public void testLocalVariablesSubExpressions() throws Throwable {       
        EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
        new EventTool().waitNoEvent(500);
        Utilities.toggleBreakpoint(eo, 104);
        new EventTool().waitNoEvent(500);
        Utilities.startDebugger();
        Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104.");
        expandNodes();
        new EventTool().waitNoEvent(700);
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);
        Utilities.getStepOverExpressionAction().perform();
        new EventTool().waitNoEvent(700);        

        Utilities.showDebuggerView(Utilities.variablesViewTitle);
        OutlineOperator outlineOp = new OutlineOperator(new TopComponentOperator(Utilities.variablesViewTitle));
        int count = 1; //line 0 is the New Watch Expression line by default
        checkOutlineTableLine(outlineOp, count++, "Before call to 'println()'", null, null);
        if (version.equals("jdk16")) {
            checkOutlineTableLine(outlineOp, count++, "Arguments", null, null);
            checkOutlineTableLine(outlineOp, count++, "Return values history", null, null);
            checkOutlineTableLine(outlineOp, count++, "return <init>()", null, null);
            checkOutlineTableLine(outlineOp, count++, "return <init>()", null, null);
            checkOutlineTableLine(outlineOp, count++, "return <init>()", null, null);   
            checkOutlineTableLine(outlineOp, count++, "return format()", "String", null);            
        }
    }
    
    /**
     * check values in Outline line
     * @param outline
     * @param lineNumber
     * @param name
     * @param type
     * @param value
     */
    protected void checkOutlineTableLine(OutlineOperator outline, int lineNumber, String name, String type, String value) {
        try {
            outline.scrollToCell(lineNumber, 0);
            org.openide.nodes.Node.Property property;
            String string = null;
            assertTrue("Node " + name + " not displayed in Local Variables view", name.equals(outline.getValueAt(lineNumber, 0).toString()));
            property = (org.openide.nodes.Node.Property)outline.getValueAt(lineNumber, 1);
            string = property.getValue().toString();
            int maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong type in Local Variables view (displayed: " + string + ", expected: " + type + ")",
                    (type == null) || type.equals(string));
            property = (org.openide.nodes.Node.Property)outline.getValueAt(lineNumber, 2);
            string = property.getValue().toString();
            maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong value in Local Variables view (displayed: " + string + ", expected: " + value + ")",
                    (type == null) || !type.equals(string));
        } catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
    
    protected boolean hasChildNodes(String nodePath, OutlineOperator outlineOp) {
        OutlineNode node = new OutlineNode(outlineOp, nodePath);
        node.select();
        return !node.isLeaf();
    }
}
