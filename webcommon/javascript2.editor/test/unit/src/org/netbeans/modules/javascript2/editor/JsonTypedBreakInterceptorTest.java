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

// XXX note this also tests indenter
public class JsonTypedBreakInterceptorTest extends JsonTestBase {

    public JsonTypedBreakInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    public void testInsertNewLine1() throws Exception {
        insertBreak("x^", "x\n^");
    }

    public void testInsertNewLine2() throws Exception {
        insertBreak("{^", "{\n    ^\n}");
    }

    public void testInsertNewLine3() throws Exception {
        insertBreak("{^\n}", "{\n    ^\n}");
    }

    public void testInsertNewLine4() throws Exception {
        insertBreak("{\n"
            + "    \"x\": {^\n"
            + "}\n",
            "{\n"
            + "    \"x\": {\n"
            + "        ^\n"
            + "    }\n"
            + "}\n");
    }

    public void testNoContComment1() throws Exception {
        if (JsTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }

    public void testNoContComment2() throws Exception {
        insertBreak("/*^\n*/", "/*\n^\n*/");
    }

    public void testNoMultilineString() throws Exception {
        insertBreak("{ \"a\" : \"abc^def\" }", "{ \"a\" : \"abc\n    ^def\" }");
    }
}
