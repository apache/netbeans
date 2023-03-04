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
package org.netbeans.test.java.editor.breadcrumbs;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.swing.JPanel;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.lib.JavaEditorTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author jprox
 */
public class Breadcrumbs extends JavaEditorTestCase {

    public Breadcrumbs(String testMethodName) {
        super(testMethodName);
    }

    private EditorOperator oper = null;

    private static final String TEST_FILE = "Breadcrumbs";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openProject("java_editor_test");
        openSourceFile("org.netbeans.test.java.editor.breadcrumbs", TEST_FILE);
        oper = new EditorOperator(TEST_FILE);
    }

    @Override
    protected void tearDown() throws Exception {
        if (oper != null) {
            oper.closeDiscard();
        }
        super.tearDown();
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(Breadcrumbs.class)                
                .enableModules(".*")
                .clusters(".*"));
    }

    public void testCompilationUnit() {
        assertEquals("[]", getBreadcrumbsAt(10, 1));
    }
    public void testClass() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs]", getBreadcrumbsAt(16, 28));
    }
    public void testField() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, x]", getBreadcrumbsAt(19, 10));
    }
    public void testMethod() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method]", getBreadcrumbsAt(22, 9));
    }
    public void testFor() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, for <font color=#707070>(int i = 0; i &lt; 10; i++)</font>]", getBreadcrumbsAt(23, 39));
    }
    public void testResource() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, try, rd]", getBreadcrumbsAt(26, 48));
    }
    public void testTry() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, try]", getBreadcrumbsAt(27, 13));
    }
    public void testCatch() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, try, catch <font color=#707070>Exception ex</font>]", getBreadcrumbsAt(29, 13));
    }
    public void testFinally() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, try, finally]", getBreadcrumbsAt(32, 13));
    }
    public void testWhile() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, while <font color=#707070>(x > 0)</font>]", getBreadcrumbsAt(36, 13));
    }
    public void testDoWhile() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, do ... while <font color=#707070>(x &lt; 10)</font>]", getBreadcrumbsAt(41, 9));
    }
    public void testIf() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, if <font color=#707070>(x == 10)</font>]", getBreadcrumbsAt(45, 1));
    }
    public void testIfElse() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, if <font color=#707070>(x == 10)</font> else]", getBreadcrumbsAt(47, 1));
    }
    public void testIfElseIf() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, if <font color=#707070>(x == 10)</font> else, if <font color=#707070>(x == 2)</font>]", getBreadcrumbsAt(49, 1));
    }
    public void testFor15() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, for <font color=#707070>(Object object : new String[]{&quot;&quot;})</font>]", getBreadcrumbsAt(53, 1));
    }
    public void testSynchronized() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, synchronized <font color=#707070>(this)</font>]", getBreadcrumbsAt(56, 1));
    }
    public void testAnonymousClass() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, Runnable]", getBreadcrumbsAt(60, 1));
    }
    public void testAnonymousClassMethod() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, Runnable, run]", getBreadcrumbsAt(63, 1));
    }
    public void testSwitch() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, switch <font color=#707070>(x)</font>]", getBreadcrumbsAt(67, 1));
    }
    public void testCase() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, switch <font color=#707070>(x)</font>, case <font color=#707070>1:</font>]", getBreadcrumbsAt(70, 1));
    }
    public void testDefaultCase() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, method, switch <font color=#707070>(x)</font>, default:]", getBreadcrumbsAt(73, 1));
    }
    public void testInner() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, Inner]", getBreadcrumbsAt(79, 1));
    }
    public void testInnerMethod() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, Inner, innerMethod]", getBreadcrumbsAt(82, 1));
    }
    public void testEnum() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, E]", getBreadcrumbsAt(88, 1));
    }
    public void testEnumConstantBody() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, E, A, E]", getBreadcrumbsAt(90, 1));
    }
    public void testEnumConstantMethod() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, E, A, E, m]", getBreadcrumbsAt(92, 1));
    }
    public void testEnumConstant() {
        assertEquals("[org.netbeans.test.java.editor.breadcrumbs.Breadcrumbs, E, B]", getBreadcrumbsAt(95, 10));
    }
    
    

    private String getBreadcrumbsAt(int row, int col) {
        oper.setCaretPosition(row, col);
        new EventTool().waitNoEvent(1000);
        Container container = getBreadcrumbsContainer();
        if (container != null) {

            String[] nodesAsArray = getNodesAsArray(container.getComponents()[0]);
            return Arrays.toString(nodesAsArray);
        } else {
            fail("Breadcrumbs sidebar not found");
        }
        return null;
    }

    private Container getBreadcrumbsContainer() {
        Container container = JComponentOperator.findContainer(oper.getWindow(), new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {                
                return comp.getClass().getName().contains("SideBarFactoryImpl$SideBar");
            }

            @Override
            public String getDescription() {
                return "Breadcrumbs Sidebar";
            }
        });
        return container;
    }

    private String[] getNodesAsArray(Object component) {
        try {
            Class<?> breadcrumbComponent = Class.forName("org.netbeans.modules.editor.breadcrumbs.BreadCrumbComponent");
            Field nodeFiled = breadcrumbComponent.getDeclaredField("nodes");
            nodeFiled.setAccessible(true);
            Object[] nodes = (Object[]) nodeFiled.get(component);
            String[] res = new String[nodes.length];
            Class<?> breadcrumbNode = Class.forName("org.netbeans.modules.editor.breadcrumbs.BreadCrumbsNodeImpl");
            for (int i = 0; i < nodes.length; i++) {
                Object node = nodes[i];
                Method declaredMethod = breadcrumbNode.getDeclaredMethod("getHtmlDisplayName");
                declaredMethod.setAccessible(true);
                String invoke = (String) declaredMethod.invoke(node);
                res[i] = invoke;
            }
            return res;
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            fail(ex);
        }
        return null;
    }

}
