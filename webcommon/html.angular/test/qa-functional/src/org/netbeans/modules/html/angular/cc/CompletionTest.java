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
package org.netbeans.modules.html.angular.cc;

import org.netbeans.modules.html.angular.GeneralAngular;
import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;

/**
 *
 * @author Vladimir Riha
 */
public class CompletionTest extends GeneralAngular {

    static final String[] tests = new String[]{
        "openProject",
        "testExpression12",
        "testExpression13",
        "testExpression17",
        "testMatchingCCExpression",
        "testAttribute30",
        "testAttribute32",
        "testAttribute33"
    };

    public CompletionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(CompletionTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("simpleProject");
        evt.waitNoEvent(2000);
        openFile("index.html", "simpleProject");
        endTest();
    }

    public void testExpression12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 12);
        endTest();
    }

    public void testExpression13() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 13);
        endTest();
    }

    public void testExpression17() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 17);
        endTest();
    }

    public void testAttribute30() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 30);
        endTest();
    }

    public void testAttribute32() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 32);
        endTest();
    }

    public void testAttribute33() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 33);
        endTest();
    }

    public void testMatchingCCExpression() {
        startTest();
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPosition(18, 63);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(500);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"cssColor", "clearContact"});
        checkCompletionDoesntContainItems(cjo, new String[]{"name", "alert"});
        endTest();
    }

}
