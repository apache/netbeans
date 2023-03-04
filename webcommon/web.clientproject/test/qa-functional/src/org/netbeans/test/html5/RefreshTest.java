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
package org.netbeans.test.html5;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class RefreshTest extends GeneralHTMLProject {

    private static final Logger LOGGER = Logger.getLogger(RefreshTest.class.getName());

    public RefreshTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(RefreshTest.class).addTest(
                        "testOpenProject",
                        "testHTMLEdit",
                        "testCSSEdit",
                        "testJavaScriptEdit",
                        "testHTMLEditDisabled",
                        "testJavaScriptEditDisabled"
                )
                .enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        RefreshTest.current_project = "simpleProject";
        openProject("simpleProject");
        setRunConfiguration("Embedded WebKit Browser", true, true);
        setProxy();
        endTest();
    }

    public void testHTMLEdit() {
        startTest();
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        EditorOperator eo = new EditorOperator("refresh.html");
        eo.setCaretPosition("relo", false);
        DomOperator dom = new DomOperator();
        String before = dom.toString();
        type(eo, "ad");
        evt.waitNoEvent(100);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.save();

        evt.waitNoEvent(3000);

        dom = new DomOperator();
        String after = dom.toString();

        assertFalse("DOM window not updated", before.equals(after));
        assertTrue("DOM does not contain new attribute", after.contains("reload"));
        endTest();
    }

    public void testJavaScriptEdit() {
        startTest();
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        openFile("refresh.js", RefreshTest.current_project);
        EditorOperator eo = new EditorOperator("refresh.js");
        eo.insert("window.setTimeout(function(){window.document.getElementById(\"el2\").innerHTML=\"<span></span>\"; }, 1000);");
        evt.waitNoEvent(100);
        eo.save();
        evt.waitNoEvent(3000);
        DomOperator dom = new DomOperator();
        String text = dom.toString();
        assertTrue("File not reloaded after JS change", text.contains("span"));
        assertTrue("File not reloaded after JS change", !text.contains("div"));
        endTest();
    }

    public void testCSSEdit() throws Exception {
        startTest();
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        openFile("refresh.css", RefreshTest.current_project);
        EditorOperator eo = new EditorOperator("refresh.css");
        eo.replace("red", "green");
        evt.waitNoEvent(100);
        eo.save();
        evt.waitNoEvent(2000);
        new DomOperator().focusElement("html|body", "0|0");
        CSSStylesOperator co = new CSSStylesOperator("body#el2.reload");
        HashMap<String, String> properties = co.getCSSProperties();
        assertTrue("CSS Styles contains old color value", properties.get("color").equals("green"));
        endTest();
    }

    public void testHTMLEditDisabled() {
        startTest();
        setRunConfiguration("Embedded WebKit Browser", false, true);
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        EditorOperator eo = new EditorOperator("refresh.html");
        eo.setCaretPosition("reload", false);
        DomOperator dom = new DomOperator();
        String before = dom.toString();
        type(eo, "x");
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(100);
        eo.save();

        evt.waitNoEvent(3000);
        dom = new DomOperator();
        String after = dom.toString();

        assertTrue("DOM window not updated", before.equals(after));
        endTest();
    }

     public void testJavaScriptEditDisabled() {
        startTest();
        setRunConfiguration("Embedded WebKit Browser", false, true);
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        openFile("refresh.js", RefreshTest.current_project);
        EditorOperator eo = new EditorOperator("refresh.js");
        eo.deleteLine(1);
        eo.insert("window.setTimeout(function(){window.document.getElementById(\"el2\").innerHTML=\"<pre></pre>\"; }, 1000);");
        evt.waitNoEvent(100);
        eo.save();
        evt.waitNoEvent(3000);
        DomOperator dom = new DomOperator();
        String text = dom.toString();
        assertFalse("File not reloaded after JS change", text.contains("pre"));
        assertTrue("File not reloaded after JS change", text.contains("span"));
        endTest();
    }
    

}
