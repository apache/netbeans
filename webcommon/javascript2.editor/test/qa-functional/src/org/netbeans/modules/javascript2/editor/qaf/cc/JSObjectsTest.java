/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class JSObjectsTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "createApplication",
        "testStringConstructor",
        "testStringSimple",
        "testRegExpConstructor",
        "testRegExpSimple",
        "testLiteralArray",
        "testCondensedArray",
        "testRegularArray",
        "testArrayConfirmedProp",
        "testArrayConfirmedMethod",
        "testBoolean",
        "testMath",
        "testMathConfirmedConstant",
        "testMathConfirmedMethod",
        "testMathHelp",
        "testDate",
        "testNumber",
        "testNumberPartial",
        "testGlobalFunctions",
        "testGlobalFunctionsPartial",
        "testGlobalHelpWindow",
        "testGlobalDateInLocal"
    };

    public JSObjectsTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(JSObjectsTest.class, tests);
    }

    public void createApplication() {
        startTest();
        JSObjectsTest.NAME_ITERATOR++;
        createPhpApplication(TEST_BASE_NAME +"jso_"+ NAME_ITERATOR);
        EditorOperator eo = createWebFile("jsocc", TEST_BASE_NAME +"jso_"+ NAME_ITERATOR, "JavaScript File");
        JSObjectsTest.currentFile = "jsocc.js";
        try {
            waitScanFinished();
        } catch (Exception e) {
            evt.waitNoEvent(3000); // fallback
        }

        cleanFile(eo);
        endTest();
    }

    public void testObject(String[] lines, String[] result) {

        EditorOperator eo = new EditorOperator(JSObjectsTest.currentFile);
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
        CompletionJListOperator.hideAll();
    }

    public void testLiteralArray() {
        startTest();
        testObject(new String[]{"var foo=[\"bar1\",\"bar2\"];", "foo."}, new String[]{"push", "pop", "concat", "indexOf", "join", "lastIndexOf", "reverse", "shift", "slice", "sort", "splice", "toString", "unshift", "valueOf"});
        endTest();
    }

    public void testCondensedArray() {
        startTest();
        testObject(new String[]{"var foo=new Array(\"bar1\",\"bar2\");", "foo."}, new String[]{"push", "pop", "concat", "indexOf", "join", "lastIndexOf", "reverse", "shift", "slice", "sort", "splice", "toString", "unshift", "valueOf"});
        endTest();
    }

    public void testRegularArray() {
        startTest();
        testObject(new String[]{"var foo=new Array();", "foo[0]=\"bar1\";", "foo."}, new String[]{"push", "pop", "concat", "indexOf", "join", "lastIndexOf", "reverse", "shift", "slice", "sort", "splice", "toString", "unshift", "valueOf"});
        endTest();
    }

    public void testBoolean() {
        startTest();
        testObject(new String[]{"var foo=new Boolean();", "foo."}, new String[]{"toString", "valueOf"});
        endTest();
    }

    public void testDate() {
        startTest();
        testObject(new String[]{"var myDate=new Date();", "myDate."}, new String[]{"getDate", "getDay", "getFullYear", "getHours", "getMilliseconds", "getMinutes", "getMonth", "getSeconds",
                    "getTime", "getTimezoneOffset", "getUTCDate", "getUTCDay", "getUTCFullYear", "getUTCHours", "getUTCMilliseconds", "getUTCMinutes", "getUTCMonth", "getUTCSeconds", "parse",
                    "setDate", "setFullYear", "setHours", "setMilliseconds", "setMinutes", "setMonth", "setSeconds", "setTime", "setUTCDate", "setUTCFullYear", "setUTCHours", "setUTCMilliseconds",
                    "setUTCMinutes", "setUTCMonth", "setUTCSeconds", "toDateString", "toISOString", "toJSON", "toLocaleDateString", "toLocaleTimeString", "toLocaleString", "toString", "toTimeString",
                    "toUTCString", "UTC", "valueOf"});
        endTest();

    }

    public void testGlobalDateInLocal() {
        startTest();
        testObject(new String[]{"var myDate=new Date(); function test(){\n", "myDate."}, new String[]{"getDate", "getDay", "getFullYear", "getHours", "getMilliseconds", "getMinutes", "getMonth", "getSeconds",
                    "getTime", "getTimezoneOffset", "getUTCDate", "getUTCDay", "getUTCFullYear", "getUTCHours", "getUTCMilliseconds", "getUTCMinutes", "getUTCMonth", "getUTCSeconds", "parse",
                    "setDate", "setFullYear", "setHours", "setMilliseconds", "setMinutes", "setMonth", "setSeconds", "setTime", "setUTCDate", "setUTCFullYear", "setUTCHours", "setUTCMilliseconds",
                    "setUTCMinutes", "setUTCMonth", "setUTCSeconds", "toDateString", "toISOString", "toJSON", "toLocaleDateString", "toLocaleTimeString", "toLocaleString", "toString", "toTimeString",
                    "toUTCString", "UTC", "valueOf"});
        endTest();

    }

    public void testMath() {
        startTest();
        testObject(new String[]{"var x= Math."}, new String[]{"E", "LN2", "LN10", "LOG2E", "LOG10E", "PI", "SQRT1_2", "SQRT2",
                    "abs", "acos", "asin", "atan", "atan2", "ceil", "cos", "exp", "floor", "log", "max", "min", "pow", "random",
                    "round", "sin", "sqrt", "tan"});
        endTest();
    }

    public void testMathConfirmedConstant() {
        startTest();
        testObjectConfirmedCompletion(new String[]{"var x = Math.LN"}, "LN10", "var x = Math.LN10");
        endTest();
    }

    public void testMathConfirmedMethod() {
        startTest();
        testObjectConfirmedCompletion(new String[]{"var x = Math.a"}, "abs", "var x = Math.abs()");
        endTest();
    }

    public void testArrayConfirmedProp() {
        startTest();
        testObjectConfirmedCompletion(new String[]{"var foo=[\"bar1\",\"bar2\"];", "foo.l"}, "length", "foo.length");
        endTest();
    }

    public void testArrayConfirmedMethod() {
        startTest();
        testObjectConfirmedCompletion(new String[]{"var foo=[\"bar1\",\"bar2\"];", "foo.p"}, "pop", "foo.pop()");
        endTest();
    }

    public void testObjectConfirmedCompletion(String[] lines, String toSelect, String expectedResult) {
        EditorOperator eo = new EditorOperator(JSObjectsTest.currentFile);
        cleanFile(eo);
        for (String line : lines) {
            type(eo, line + "\n");
        }
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() - 1);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem(toSelect);
        eo.pressKey(java.awt.event.KeyEvent.VK_ENTER);
        assertEquals("Incorrect completion", expectedResult, (eo.getText(eo.getLineNumber())).trim());

    }

    public void testMathHelp() {
        startTest();

        EditorOperator eo = new EditorOperator(JSObjectsTest.currentFile);
        cleanFile(eo);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(1);
        type(eo, "x = Math.a");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("abs");

        WindowOperator jdDoc = new WindowOperator(0);
        JEditorPaneOperator jeEdit = new JEditorPaneOperator(jdDoc);
        String sCompleteContent = jeEdit.getText();
        String toFind = "Returns";
        if (-1 == sCompleteContent.indexOf(toFind)) {
            System.out.println(">>>" + sCompleteContent + "<<<");
            fail("Unable to find part of required documentation: \"" + toFind + "\"");
        }

        endTest();
    }

    public void testNumber() {
        startTest();
        testObject(new String[]{"var x= new Number(1);", "x."}, new String[]{"MAX_VALUE", "MIN_VALUE", "NEGATIVE_INFINITY", "POSITIVE_INFINITY",
                    "toExponential", "toFixed", "toPrecision", "toString", "valueOf"});
        endTest();
    }

    public void testNumberPartial() {
        startTest();
        testObject(new String[]{"var x= new Number(1);", "x.to"}, new String[]{"toExponential", "toFixed", "toPrecision", "toString"});
        endTest();
    }

    public void testStringConstructor() {
        startTest();
        testObject(new String[]{"var x= new String(\"a\");", "x."}, new String[]{"length", "charAt", "charCodeAt", "concat",
                    "fromCharCode", "indexOf", "lastIndexOf", "match", "replace", "search", "slice", "split", "substr", "toLowerCase",
                    "toUpperCase", "valueOf", "anchor", "big", "blink", "bold", "fixed", "fontcolor", "fontsize", "italics", "link", "small",
                    "strike", "sub", "sup"});
        endTest();
    }

    public void testStringSimple() {
        startTest();
        testObject(new String[]{"var x= \"a\";", "x."}, new String[]{"length", "charAt", "charCodeAt", "concat",
                    "fromCharCode", "indexOf", "lastIndexOf", "match", "replace", "search", "slice", "split", "substr", "toLowerCase",
                    "toUpperCase", "valueOf", "anchor", "big", "blink", "bold", "fixed", "fontcolor", "fontsize", "italics", "link", "small",
                    "strike", "sub", "sup"});
        endTest();
    }

    public void testRegExpConstructor() {
        startTest();
        testObject(new String[]{"var x= new RegExp(\"e\");", "x."}, new String[]{"global", "ignoreCase", "lastIndex", "multiline",
                    "source", "compile", "exec", "test"});
        endTest();
    }

    public void testRegExpSimple() {
        // issue 215781
        startTest();
        testObject(new String[]{"var x= /^e/;", "x."}, new String[]{"global", "ignoreCase", "lastIndex", "multiline",
                    "source", "compile", "exec", "test"});
        endTest();
    }

    public void testGlobalFunctions() {
        startTest();
        testObject(new String[]{" "}, new String[]{"document", "window", "decodeURI", "decodeURIComponent", "encodeURI", "encodeURIComponent",
                    "escape", "eval", "isFinite", "isNaN", "Number", "parseFloat", "parseInt", "String", "unescape"});
        endTest();
    }

    public void testGlobalFunctionsPartial() {
        startTest();
        testObject(new String[]{" d"}, new String[]{"document", "decodeURI", "decodeURIComponent"});
        endTest();
    }

    public void testGlobalHelpWindow() {
        startTest();

        EditorOperator eo = new EditorOperator(JSObjectsTest.currentFile);
        cleanFile(eo);
        type(eo, "\n\n");
        eo.setCaretPositionToLine(1);
        type(eo, "dec");

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(100);
        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("decodeURI");

        WindowOperator jdDoc = new WindowOperator(0);
        JEditorPaneOperator jeEdit = new JEditorPaneOperator(jdDoc);
        String sCompleteContent = jeEdit.getText();
        String toFind = "Returns";
        if (-1 == sCompleteContent.indexOf(toFind)) {
            System.out.println(">>>" + sCompleteContent + "<<<");
            fail("Unable to find part of required documentation: \"" + toFind + "\"");
        }

        endTest();
    }
}