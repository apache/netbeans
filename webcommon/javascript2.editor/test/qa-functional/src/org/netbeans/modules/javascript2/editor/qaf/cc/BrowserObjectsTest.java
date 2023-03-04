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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class BrowserObjectsTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "createApplication",
        "testWindowAssigned",
        "testWindowSimple",
        "testLocationViaWindow",
        "testLocationSimple",
        "testOverridenObject",
        "testNavigatorInFunction",
        "testScreenSimple",
        "testHistorySimple"
    };

    public BrowserObjectsTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(BrowserObjectsTest.class, tests);
    }

    public void createApplication() {
        startTest();
        BrowserObjectsTest.NAME_ITERATOR++;
        createPhpApplication(TEST_BASE_NAME + "bo_" + NAME_ITERATOR);
        EditorOperator eo = createWebFile("bocc", TEST_BASE_NAME + "bo_" + NAME_ITERATOR, "JavaScript File");
        BrowserObjectsTest.currentFile = "bocc.js";
        try {
            waitScanFinished();
        } catch (Exception e) {
            evt.waitNoEvent(3000); // fallback
        }

        cleanFile(eo);
        endTest();
    }

    public void testObject(String[] lines, String[] result) {

        EditorOperator eo = new EditorOperator(BrowserObjectsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);
        for (String line : lines) {
            type(eo, line + "\n");
        }
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() - 1);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, result);
        completion.listItself.hideAll();
    }

    public void testWindowAssigned() {
        startTest();
        testObject(new String[]{"var myW=window."}, new String[]{"closed", "defaultStatus", "document", "frames", "history", "innerHeight", "innerWidth", "length",
                    "location", "name", "navigator", "opener", "outerHeight", "outerWidth", "pageXOffset", "pageYOffset", "parent", "screen", "screenLeft", "screenRight",
                    "screenX", "screenY", "self", "status", "top", "alert", "blur", "clearInterval", "clearTimeout", "close", "confirm", "focus", "moveBy",
                    "moveTo", "open", "print", "prompt", "resizeBy", "resizeTo", "scroll", "scrollBy", "scrollTo", "setInterval", "setTimeout"});
        endTest();
    }

    public void testWindowSimple() {
        startTest();
        testObject(new String[]{"window."}, new String[]{"closed", "defaultStatus", "document", "frames", "history", "innerHeight", "innerWidth", "length",
                    "location", "name", "navigator", "opener", "outerHeight", "outerWidth", "pageXOffset", "setInterval", "setTimeout"});
        endTest();
    }

    public void testLocationViaWindow() {
        startTest();
        testObject(new String[]{"window.location."}, new String[]{"hash", "host", "hostname", "href", "pathname", "port", "protocol", "search", "assign", "reload", "replace"});
        endTest();
    }

    public void testLocationSimple() {
        startTest();
        testObject(new String[]{"location."}, new String[]{"hash", "host", "hostname", "href", "pathname", "port", "protocol", "search", "assign", "reload", "replace"});
        endTest();
    }

    public void testOverridenObject() {
        startTest();
        EditorOperator eo = new EditorOperator(BrowserObjectsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);
        type(eo, "var location={foo:1, bar:function(){}};\nlocation.");
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"foo", "bar"});
        endTest();
    }

    public void testNavigatorInFunction() {
        startTest();
        testObject(new String[]{"function print(){\n", "console.log(\"Browser \"+navigator."}, new String[]{"appCodeName", "appName", "appVersion", "cookieEnabled", "platform", "userAgent", "javaEnabled"});
        endTest();
    }

    public void testScreenSimple() {
        startTest();
        testObject(new String[]{"screen."}, new String[]{"availHeight", "availWidth", "colorDepth", "height", "pixelDepth", "width"});
        endTest();
    }

    public void testHistorySimple() {
        startTest();
        testObject(new String[]{"history."}, new String[]{"length", "back", "forward", "go"});
        endTest();
    }
}
