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

package org.netbeans.modules.javascript2.editor;

import org.netbeans.modules.csl.api.Formatter;


public class JsonTypedTextInterceptorTest extends JsonTestBase {

    public JsonTypedTextInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testNoMatchInStrings() throws Exception {
        insertChar("\"^\"", '\'', "\"'^\"");
        insertChar("\"^\"", '[', "\"[^\"");
        insertChar("\"^\"", '(', "\"(^\"");
        insertChar("\"^)\"", ')', "\")^)\"");
    }

    public void testNoSingleQuotes1() throws Exception {
        insertChar("{ ^ }", '\'', "{ '^ }");
    }

    public void testNoSingleQuotes2() throws Exception {
        insertChar("{ '^ }", '\'', "{ ''^ }");
    }

    public void testDoubleQuotes1() throws Exception {
        insertChar("{ ^ }", '"', "{ \"^\" }");
    }

    public void testDoubleQuotes2() throws Exception {
        insertChar("{ \"^\" }", '"', "{ \"\"^ }");
    }

    public void testDoubleQuotes3() throws Exception {
        insertChar("{ \"^\" }", 'a', "{ \"a^\" }");
    }

    public void testDobuleQuotes4() throws Exception {
        insertChar("{ \"\\^\" }", '"', "{ \"\\\"^\" }");
    }

    public void testBrackets1() throws Exception {
        insertChar("{ ^ }", '[', "{ [^] }");
    }

    public void testBrackets2() throws Exception {
        insertChar("{ [^] }", ']', "{ []^ }");
    }

    public void testBrackets3() throws Exception {
        insertChar("{ [^] }", 'a', "{ [a^] }");
    }

    public void testBrackets4() throws Exception {
        insertChar("{ [^] }", '[', "{ [[^]] }");
    }

    public void testBrackets5() throws Exception {
        insertChar("{ [[^]] }", ']', "{ [[]^] }");
    }

    public void testBrackets6() throws Exception {
        insertChar("{ [[]^] }", ']', "{ [[]]^ }");
    }

    public void testBrace1() throws Exception {
        insertChar("{ \"x\":{^} }", '}', "{ \"x\":{}^ }");
    }

    public void testNoParens1() throws Exception {
        insertChar("{ ^ }", '(', "{ (^ }");
    }

    public void testNoParens2() throws Exception {
        insertChar("{ (^) }", ')', "{ ()^) }");
    }
}
