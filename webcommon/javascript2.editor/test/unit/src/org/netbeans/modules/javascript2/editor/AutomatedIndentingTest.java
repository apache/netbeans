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

/**
 *
 * @author Petr Hejl
 */
public class AutomatedIndentingTest extends JsTestBase {

    public AutomatedIndentingTest(String testName) {
        super(testName);
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    public void testIssue234083() throws Exception {
        insertChar("switch(x){\n    case 1:\n  case^\n}", ' ', "switch(x){\n    case 1:\n    case ^\n}");
    }

    public void testIssue231017() throws Exception {
        insertChar("$('div')\n^", '.', "$('div')\n        .^");
    }

    public void testIssue182420() throws Exception {
        insertChar("if (foo)\n    ^", '{', "if (foo)\n{^");
    }
}
