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
package org.netbeans.modules.javascript2.requirejs.cc;

import java.awt.event.InputEvent;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.requirejs.GeneralRequire;

/**
 *
 * @author Vladimir Riha
 */
public class ScriptCompletionTest extends GeneralRequire {

    public ScriptCompletionTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ScriptCompletionTest.class).addTest(
                        "openProject",
                        "testNonScript",
                        "testScript",
                        "testDataMainFolderPath",
                        "testDataMainPath"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        ScriptCompletionTest.currentProject = "SimpleRequire";
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleRequire");
        openFile("index.html", "SimpleRequire");
        endTest();
    }

    public void testNonScript() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(13, 11);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlAttrItems(cjo, new String[]{"data-bind"});
        checkCompletionDoesntContainHtmlAttrItems(cjo, new String[]{"data-main"});
        completion.listItself.hideAll();
        eo.close(false);
        endTest();
    }

    public void testScript() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(16, 17);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlAttrItems(cjo, new String[]{"data-bind", "data-main"});
        completion.listItself.hideAll();
        eo.close(false);
        endTest();
    }

    public void testDataMainPath() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(16, 17);
        type(eo, "data-main=");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionHtmlAttrItems(cjo, new String[]{"../", "sample.js", "js/"});
        checkCompletionDoesntContainHtmlAttrItems(cjo, new String[]{"app", "main.js"});
        completion.listItself.hideAll();
        eo.close(false);
        endTest();
    }

    public void testDataMainFolderPath() {
        startTest();
        EditorOperator eo = openFile("index.html", "SimpleRequire");
        eo.setCaretPosition(16, 17);
        type(eo, "data-main=js/");
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        completion.hideAll();

        checkCompletionHtmlAttrItems(cjo, new String[]{"app/", "bbb/", "main.js"});
        checkCompletionDoesntContainHtmlAttrItems(cjo, new String[]{"sample.js"});

        eo.typeKey(' ', InputEvent.CTRL_MASK);

        completion = getCompletion();
        cjo = completion.listItself;
        final CompletionJListOperator op = cjo;
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    op.clickOnItem(5, 2);
                }
            });
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        evt.waitNoEvent(1000);
        assertTrue("File badly inserted: "+eo.getText(eo.getLineNumber()), eo.getText(eo.getLineNumber()).contains("data-main=\"js/main\""));
        eo.close(false);
        endTest();
    }

}
