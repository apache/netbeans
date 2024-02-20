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
package org.netbeans.modules.html.editor.hints.other;

import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author Christian Lenz
 */
public class AddMissingAltAttributeRuleTest extends TestBase {

    public AddMissingAltAttributeRuleTest(String testName) {
        super(testName);
    }

    private Rule createRule() {
        return AddMissingAltAttributeRule.getInstance();
    }

    public void testProvideTextAlternativeHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/addMissingAltAttribute1.html", "<img^");
    }

    //~ Fix
    public void testProvideTextAlternativeHintFix_01() throws Exception {
        applyHint(
            this,
            createRule(),
            "testfiles/hints/addMissingAltAttribute1.html",
            "<img^",
            "Provide Text Alternatives"
        );
    }
}
