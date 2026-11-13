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

    // ===== Basic img tests =====

    public void testProvideTextAlternativeHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/other/addMissingAltAttribute1.html", "<img^");
    }

    public void testProvideTextAlternativeHintFix() throws Exception {
        applyHint(
            this,
            createRule(),
            "testfiles/hints/other/addMissingAltAttribute1.html",
            "<img^",
            "Provide Text Alternatives"
        );
    }

    // ===== Multiple images tests =====

    public void testMultipleImagesOnly2ImageHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/other/addMissingAltAttribute2.html", "<img src=\"image2.png\"^");
    }

    public void testMultipleImagesOnly2ImageFix() throws Exception {
        applyHint(
            this,
            createRule(),
            "testfiles/hints/other/addMissingAltAttribute2.html",
            "<img src=\"image2.png\"^",
            "Provide Text Alternatives"
        );
    }

    public void testMultipleImagesFirstHasAltNoHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/other/addMissingAltAttribute2.html", "<img src=\"image1.png\"^");
    }

    // ===== Applet tests =====

    public void testAppletHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/other/addMissingAltAttributeApplet.html", "<applet^");
    }

    public void testAppletFix() throws Exception {
        applyHint(
            this,
            createRule(),
            "testfiles/hints/other/addMissingAltAttributeApplet.html",
            "<applet^",
            "Provide Text Alternatives"
        );
    }

    // ===== Area tests =====

    public void testAreaHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/other/addMissingAltAttributeArea.html", "<area^");
    }

    public void testAreaFix() throws Exception {
        applyHint(
            this,
            createRule(),
            "testfiles/hints/other/addMissingAltAttributeArea.html",
            "<area^",
            "Provide Text Alternatives"
        );
    }

    // ===== Multiline img tests =====

    public void testMultilineHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/other/addMissingAltAttributeMultiline.html", "<img src=\"test.png\"^");
    }

    public void testMultilineFix() throws Exception {
        applyHint(
            this,
            createRule(),
            "testfiles/hints/other/addMissingAltAttributeMultiline.html",
            "<img src=\"test.png\"^",
            "Provide Text Alternatives"
        );
    }

    // ===== Existing alt attribute tests (no hint expected) =====

    public void testExistingAltNoHint() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/other/addMissingAltAttributeExisting.html", "<img^");
    }
}
