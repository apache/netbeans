/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.completion;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport.Match;
import org.netbeans.modules.html.parser.HtmlDocumentation;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class Html5CompletionQueryTest extends HtmlCompletionQueryTest {

    private static final String HTML5_DOCTYPE = "<!doctype html>";

    public Html5CompletionQueryTest(String name) throws IOException, BadLocationException {
        super(name);
    }

    @Override
    protected HtmlVersion getExpectedVersion() {
        return HtmlVersion.HTML5;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML5;
        HtmlDocumentation.setupDocumentationForUnitTests();
    }

    public static Test Xsuite() throws IOException, BadLocationException {
        System.err.println("Beware, only selected tests runs!!!");
        TestSuite suite = new TestSuite();
        suite.addTest(new Html5CompletionQueryTest("testEndTagAutocompletion"));
        return suite;
    }

    @Override
    public void testTagAttributeValues() throws BadLocationException, ParseException {
        assertItems("<div contenteditable=\"|\"", arr("false"), Match.CONTAINS, 22);
        //           012345678901234567890 12 34567
        assertItems("<div contenteditable=\"tr|\"", arr("true"), Match.CONTAINS, 22);
        assertItems("<div contenteditable=\"true|\"", arr("true"), Match.EXACT, 22);

    }
    
    @Override
      public void testCompleteTagAttributeValues() throws BadLocationException, ParseException {
        assertCompletedText("<div contenteditable=\"|\"", "true", "<div contenteditable=\"true|\"");
        assertCompletedText("<div contenteditable=\"tr|\"", "true", "<div contenteditable=\"true|\"");

        //regression test - issue #161852
        assertCompletedText("<div contenteditable=\"|\"", "false", "<div contenteditable=\"false|\"");

        //test single quote
        assertCompletedText("<div contenteditable='|'", "false", "<div contenteditable='false'|");

        //test values cc without quotation
        assertCompletedText("<div contenteditable=|", "false", "<div contenteditable=false|");
        assertCompletedText("<div contenteditable=tr|", "true", "<div contenteditable=true|");
    }

    
    @Override
    public void testNoEndTagAutocompletion() {
        //disabled for html5, the end tag autocompletion works even for unknown tags
    }

    @Override
    public void testBooleanAttributes() throws BadLocationException, ParseException {
        System.err.println("testBooleanAttributes test is disabled due to a bug!");
        //disabled, fails since there's no information if an attribute is empty or not so the
        //equal sign plus quotations is generated by the completion even if the value is forbidden
    }

    public void testIssue193268() throws IOException, BadLocationException, ParseException {
        TestSource sap = getTestSource("issue193268.html", false);
        assertItems(sap.getCode(), new String[]{"id", "href"} , Match.CONTAINS);
    }
    
    //Bug 197608 - Non-html tags offered as closing tags using code completion 
    public void testIssue197608() throws BadLocationException, ParseException {
        assertItems("<div></di|", arr("div"), Match.EXACT);
        assertCompletedText("<div></di|", "/div", "<div></div>|");
    }
    
    //Bug 197614 - Problem with HTML4 & code completion - non matching tags offered
    public void testIssue197614() throws BadLocationException, ParseException {
        assertItems("<table><tr><td></ta|", arr("table"), Match.EXACT);
    }

    @Override
    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        super.assertItems(HTML5_DOCTYPE + documentText,
                expectedItemsNames,
                type,
                expectedAnchor != -1 ? HTML5_DOCTYPE.length() + expectedAnchor : -1);
    }
}
