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
package org.netbeans.modules.javascript2.editor.qaf.jsdoc;

import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class CompletionTest extends GeneralJavaScript {

    public CompletionTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CompletionTest.class).addTest(
                        "openProject",
                        "testReturns",
                        "testReturnsComposed",
                        "testReturnsObject",
                        "testParams"
                ).enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("completionTest");
        evt.waitNoEvent(5000);
        openFile("jsdoc.js");
    }

    public void openFile(String fileName) {
        Logger.getLogger(CompletionTest.class.getName()).info("Opening file " + fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode("completionTest");
        Node node = new Node(rootNode, "Source Files|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    public void testReturns() {
        startTest();
        openFile("jsdoc.js");
        doTest(new EditorOperator("jsdoc.js"), 7);
        endTest();
    }
    public void testReturnsComposed() {
        startTest();
        doTest(new EditorOperator("jsdoc.js"), 23);
        endTest();
    }
    public void testReturnsObject() {
        startTest();
        doTest(new EditorOperator("jsdoc.js"), 36);
        endTest();
    }
    public void testParams() {
        startTest();
        doTest(new EditorOperator("jsdoc.js"), 48);
        endTest();
    }

}
