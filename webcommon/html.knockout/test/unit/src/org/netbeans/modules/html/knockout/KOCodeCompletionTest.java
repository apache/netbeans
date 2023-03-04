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
package org.netbeans.modules.html.knockout;

import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;

/**
 *
 * @author Petr Pisl
 */
public class KOCodeCompletionTest extends JsCodeCompletionBase {

    public KOCodeCompletionTest(String testName) {
        super(testName);
    }
    
    public void testForEach() throws Exception {
        checkCompletion("completion/foreach/index.html", "            <div data-bind=\"text: ^ , css: jmeno == 'pepa' ? 'jouda' :", false);
    }

    public void testForEachAlias() throws Exception {
        checkCompletion("completion/foreachAlias/index.html", "                <span data-bind=\"text: ^\"></span>", false);
    }

    public void testWith() throws Exception {
        checkCompletion("completion/with/index.html", "            <div data-bind=\"text: ^\"></div>", false);
    }

    public void testIssue231569() throws Exception {
        checkCompletion("completion/issue231569/index.html", "                <input data-bind='value: userNameToAdd, valueUpdate: \"keyup\", css: { invalid: ^ }' /></input>", false);
    }

    public void testTemplate() throws Exception {
        checkCompletion("completion/template/index.html", "            <h3 data-bind=\"text: ^\"></h3>", false);
    }

    public void testTemplateForEach() throws Exception {
        checkCompletion("completion/templateForEach/index.html", "    <h3 data-bind=\"text: ^\"></h3>", false);
    }

    public void testTemplateForEachAlias() throws Exception {
        checkCompletion("completion/templateForEachAlias/index.html", "    <h3 data-bind=\"text: simple.^ \"></h3>", false);
    }

    public void testTemplateInner() throws Exception {
        checkCompletion("completion/templateInner/index.html", "        <strong data-bind=\"text: ^\"></strong>", false);
    }
}
