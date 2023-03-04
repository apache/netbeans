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

/**
 * Test to cover most of the JSP Editor Test Specification
 *
 * @author Vladimir Riha
 */
public class FormattingSanityTest extends GeneralJSP {

    public FormattingSanityTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(FormattingSanityTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(conf.addTest(
                "openProject",
                "testBasicIndendation").enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        FormattingSanityTest.current_project = "sampleJSP";
        openProject(FormattingSanityTest.current_project);
        resolveServer(FormattingSanityTest.current_project);
        openFile("test.jsp", FormattingSanityTest.current_project);
        EditorOperator eo = new EditorOperator("test.jsp");
        FormattingSanityTest.original_content = eo.getText();
        endTest();
    }

    public void testBasicIndendation() {
        startTest();
        EditorOperator eo = new EditorOperator("test.jsp");
        eo.setCaretPosition(15, 9);
        type(eo, "<a>");
        evt.waitNoEvent(100);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(200);
        eo.pressKey(KeyEvent.VK_ENTER);
        eo.pressKey(KeyEvent.VK_ENTER);
        evt.waitNoEvent(200);
        assertEquals("Cursor at wrong position", 342, eo.txtEditorPane().getCaretPosition());
        endTest();
    }

    @Override
    public void tearDown() throws Exception {
        EditorOperator eo = new EditorOperator("test.jsp");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(KeyEvent.VK_DELETE);
        eo.insert(FormattingSanityTest.original_content);
        eo.save();
        evt.waitNoEvent(1000);
    }
}
