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
package org.netbeans.test.jsf.editor;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class NavigationTest extends GeneralJSF {

    public NavigationTest(String args) {
        super(args);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(NavigationTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(
                conf.addTest(
                        "testOpenProject",
                        "testFacesComponent",
                        "testFacesComponentNamed",
                        "testUIIncludeFolder",
                        "testUIInclude",
                        "testCompositeComponent",
                        "testFacesComponentNamed",
                        "testManagedBean",
                        "testManagedBeanProperty",
                        "testManagedBeanProperty2",
                        "testInnerManagedBean",
                        "testInnerManagedBeanProperty"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        NavigationTest.current_project = "sampleJSF22";
        openProject(NavigationTest.current_project);
        resolveServer(NavigationTest.current_project);
        openFile("navigate.xhtml", NavigationTest.current_project);
        
        // workaround for issue 239455
        EditorOperator eo = new EditorOperator("navigate.xhtml");
        eo.setCaretPosition(4, 46);
        type(eo, " ");
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_S, 2)).performShortcut(eo);
        evt.waitNoEvent(500);
        endTest();
    }

    public void testFacesComponent() {
        startTest();
        navigate("navigate.xhtml", "NewFacesComponent.java", 12, 23, 1, 1);
        endTest();
    }

    public void testFacesComponentNamed() {
        startTest();
        navigate("navigate.xhtml", "NewFacesComponent1.java", 15, 17, 1, 1);
        endTest();
    }

    public void testUIIncludeFolder() {
        startTest();
        navigate("navigate.xhtml", "inc.xhtml", 18, 34, 1, 1);
        endTest();
    }

    public void testUIInclude() {
        startTest();
        navigate("navigate.xhtml", "index.xhtml", 19, 29, 1, 1);
        endTest();
    }

    public void testCompositeComponent() {
        startTest();
        navigate("navigate.xhtml", "out.xhtml", 21, 15, 1, 1);
        endTest();
    }

    public void testManagedBean() {
        startTest();
        navigate("navigate.xhtml", "SimpleBean.java", 21, 15, 51, 1);
        endTest();
    }

    public void testManagedBeanProperty() {
        startTest();
        navigate("navigate.xhtml", "SimpleBean.java", 25, 23, 58, 5);
        endTest();
    }

    public void testManagedBeanProperty2() {
        startTest();
        navigate("navigate.xhtml", "SimpleBean.java", 27, 26, 74, 5);
        endTest();
    }

    public void testInnerManagedBean() {
        startTest();
        navigate("navigate.xhtml", "SimpleBean.java", 23, 23, 66, 5);
        endTest();
    }
    public void testInnerManagedBeanProperty() {
        startTest();
        navigate("navigate.xhtml", "InnerBean.java", 29, 28, 57, 5);
        endTest();
    }

    public void navigate(String fromFile, String toFile, int fromLine, int fromColumn, int toLine, int toColumn) {
        EditorOperator eo = new EditorOperator(fromFile);
        eo.setCaretPosition(fromLine, fromColumn);
        evt.waitNoEvent(200);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_B, 2)).performShortcut(eo);
        evt.waitNoEvent(500);
        try {
            EditorOperator ed = new EditorOperator(toFile);
            int position = ed.txtEditorPane().getCaretPosition();
            ed.setCaretPosition(toLine, toColumn);
            int expectedPosition = ed.txtEditorPane().getCaretPosition();
            assertTrue("Incorrect caret position. Expected position " + expectedPosition + " but was " + position, position == expectedPosition);
            if (!fromFile.equals(toFile)) {
                ed.close(false);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
