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
package org.netbeans.modules.javascript2.jquery;

import org.netbeans.modules.javascript2.editor.JsCodeCompletionBase;

/**
 *
 * @author Petr Hejl, Petr Pisl
 */
public class JQueryCodeCompletionTest extends JsCodeCompletionBase {

    public JQueryCodeCompletionTest(String testName) {
        super(testName);
    }

    public void testProperty01() throws Exception {
        checkCompletion("testfiles/completion/jQueryFragment01.js", "^jQuery.event.customEvent.test();", false);
    }

    public void testProperty02() throws Exception {
        checkCompletion("testfiles/completion/jQueryFragment01.js", "jQuery.^event.customEvent.test();", false);
    }

    public void testProperty03() throws Exception {
        checkCompletion("testfiles/completion/jQueryFragment01.js", "jQuery.event.^customEvent.test();", false);
    }

    public void testProperty04() throws Exception {
        checkCompletion("testfiles/completion/jQueryFragment01.js", "jQuery.event.customEvent.^test();", false);
    }

    public void testProperty05() throws Exception {
        checkCompletion("testfiles/completion/jQueryFragment01.js", "jQuery.ajaxStart().add^Class();", false);
    }

    public void testIssue217123() throws Exception {
        checkCompletion("testfiles/completion/issue217123.html", "$(\"#text\").ani^", false);
    }

    public void testIssue217450() throws Exception {
        checkCompletion("testfiles/completion/issue217450.js", "$(\"#text\").^", false);
    }

    public void testMethods01() throws Exception {
        checkCompletion("testfiles/completion/jQuery/simple.js", "jQuery('#test').a^ddClass('.myClass');", false);
    }

    public void testMethods02() throws Exception {
        checkCompletion("testfiles/completion/jQuery/simple.js", "$('#test').ad^dClass('.myClass');", false);
    }

    public void testNewFile() throws Exception {
        checkCompletion("testfiles/completion/jQuery/newFile.js", "jQuery('#f-emblem').a^ddClass('.has-menu');", false);
    }

    public void testNewFile02() throws Exception {
        checkCompletion("testfiles/completion/jQuery/newFile.js", "jQuery('#f-emblem').addClass('.has-menu').a^dd(':checked');", false);
    }

    public void testIssue223963() throws Exception {
        checkCompletionDocumentation("testfiles/completion/jQuery/issue223963.js", "$(document).a^", false, "addClass");
    }

    public void testIssue223060() throws Exception {
        checkCompletion("testfiles/completion/jQuery/issue223060.js", "form.^", false);
    }
    
    public void testIssue235647() throws Exception {
        checkCompletion("testfiles/completion/issue235647.js", "$.po^", false);
    }
    
    public void testIssue249169() throws Exception {
        checkCompletion("testfiles/completion/issue249169.js", "if ($(\":an^\")) {", false);
    }
}
