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
package org.netbeans.modules.javascript2.knockout;

import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;

/**
 *
 * @author Petr Hejl
 */
public class KnockoutCodeCompletionTest extends JsCodeCompletionBase {

    public KnockoutCodeCompletionTest(String testName) {
        super(testName);
    }

    public void testFull1() throws Exception {
        checkCompletion("testfiles/completion/full.js", "ko.^", true);
    }

    public void testSimple1() throws Exception {
        checkCompletion("testfiles/completion/simple.js", "ko.observable(\"Tom\").^", true);
    }

    public void testSimple2() throws Exception {
        checkCompletion("testfiles/completion/simple.js", "ko.observableArray(\"Tom\").^", true);
    }

}
