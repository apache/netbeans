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
package org.netbeans.performance.j2se.menus;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of popup menu on Source Editor pane.
 *
 * @author mmirilovic@netbeans.org
 */
public class SourceEditorPopupMenuTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private static String fileName;

    /**
     * Creates a new instance of SourceEditorPopupMenu
     *
     * @param testName test name
     */
    public SourceEditorPopupMenuTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Creates a new instance of SourceEditorPopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public SourceEditorPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(SourceEditorPopupMenuTest.class)
                .suite();
    }

    public void testPopupInTxt() {
        fileName = "textfile.txt";
        expectedTime = 200;
        doMeasurement();
    }

    public void testPopupInXml() {
        fileName = "xmlfile.xml";
        expectedTime = 200;
        doMeasurement();
    }

    public void testPopupInJava() {
        fileName = "Main.java";
        expectedTime = 500;
        doMeasurement();
    }

    @Override
    public void initialize() {
        MainWindowOperator.getDefault().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        Node fileNode = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);

        if (fileName.endsWith("xml")) {
            new EditAction().performAPI(fileNode);
        } else {
            new OpenAction().performAPI(fileNode);
        }
        editor = new EditorOperator(fileName);
        waitNoEvent(2000);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        editor.pushKey(java.awt.event.KeyEvent.VK_F10, java.awt.event.KeyEvent.SHIFT_MASK);
        return new JPopupMenuOperator();
    }

    @Override
    public void close() {
        editor.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    @Override
    public void shutdown() {
        MainWindowOperator.getDefault().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        EditorOperator.closeDiscardAll();
    }
}
