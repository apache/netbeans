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
package org.netbeans.test.syntax;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import javax.swing.KeyStroke;

/**
 *
 * @author Vladimir Riha
 */
public class MiscellaneousSanityTest extends GeneralJSP {

    public MiscellaneousSanityTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(MiscellaneousSanityTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(conf.addTest(
                "openProject",
                "testNavigateForward",
                "testNavigateInclude",
                "testNavigateTaglib",
                "testCommentHTML",
                "testUncommentHTML",
                "testCommentScriptlet",
                "testUncommentScriptlet"
        ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        MiscellaneousSanityTest.current_project = "sampleJSP";
        openProject(MiscellaneousSanityTest.current_project);
        resolveServer(MiscellaneousSanityTest.current_project);
        openFile("nav.jsp", MiscellaneousSanityTest.current_project);
        endTest();
    }

    public void testNavigateForward() {
        startTest();
        navigate("nav.jsp", "newtag_file.tag", 9, 44, 1, 1);
        endTest();
    }

    public void testNavigateInclude() {
        startTest();
        navigate("nav.jsp", "index.jsp", 10, 30, 1, 1);
        endTest();
    }

    public void testNavigateTaglib() {
        startTest();
        navigate("nav.jsp", "newtag_file.tag", 12, 19, 1, 1);
        endTest();
    }

    public void testCommentHTML() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(11, 18);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect HTML comment", "        <!--<h1>Hello World!</h1>-->\n", eo.getText(eo.getLineNumber()));
        endTest();
    }
    public void testUncommentHTML() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(11, 18);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect HTML comment", "        <h1>Hello World!</h1>\n", eo.getText(eo.getLineNumber()));
        endTest();
    }

    
    public void testCommentScriptlet() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(14, 23);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect scriptlet comment", "//            String a = \"\";\n", eo.getText(eo.getLineNumber()));
        endTest();
    }
    public void testUncommentScriptlet() {
        startTest();
        EditorOperator eo = new EditorOperator("nav.jsp");
        eo.setCaretPosition(14, 23);
        eo.typeKey('/', InputEvent.CTRL_MASK);
        assertEquals("Incorrect scriptlet comment", "            String a = \"\";\n", eo.getText(eo.getLineNumber()));
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
