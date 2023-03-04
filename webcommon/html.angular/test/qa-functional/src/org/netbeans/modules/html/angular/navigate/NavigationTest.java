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
package org.netbeans.modules.html.angular.navigate;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.html.angular.GeneralAngular;

/**
 *
 * @author Vladimir Riha
 */
public class NavigationTest extends GeneralAngular {

    static final String[] tests = new String[]{
        "openProject",
        "testNavigationController",
        "testNavigationExpression",
        "testNavigationModel",
        "testNavigationClick",
        "testNavigationRepeat",
        "testNavigationRepeat2",
        "testNavigationExpression2"
    };

    public NavigationTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(NavigationTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("simpleProject");
        evt.waitNoEvent(2000);
        openFile("index.html", "simpleProject");
        endTest();
    }

    public void testNavigationController() {
        startTest();
        navigate("index.html", "file.js", 8, 37, 1, 10);
        endTest();
    }

    public void testNavigationExpression() {
        startTest();
        navigate("index.html", "file.js", 9, 21, 2, 12);
        endTest();
    }

    public void testNavigationModel() {
        startTest();
        navigate("index.html", "file.js", 10, 50, 2, 12);
        endTest();
    }

    public void testNavigationClick() {
        startTest();
        navigate("index.html", "file.js", 11, 38, 8, 12);
        endTest();
    }

    public void testNavigationRepeat() {
        startTest();
        navigate("index.html", "file.js", 16, 48, 4, 12);
        endTest();
    }

    public void testNavigationRepeat2() {
        startTest();
        navigate("index.html", "index.html", 18, 43, 16, 32);
        endTest();
    }

    public void testNavigationExpression2() {
        startTest();
        navigate("index.html", "file.js", 29, 21, 25, 12);
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
