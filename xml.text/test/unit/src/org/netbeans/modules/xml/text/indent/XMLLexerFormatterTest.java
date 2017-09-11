/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.text.indent;

import org.netbeans.modules.xml.text.AbstractTestCase;
import junit.framework.*;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.editor.BaseDocument;

/**
 * Formatting related tests based on new formatter. See XMLLexerFormatter.
 * 
 * @author Samaresh (samaresh.panda@sun.com)
 */
public class XMLLexerFormatterTest extends AbstractTestCase {

    public XMLLexerFormatterTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLLexerFormatterTest("testFormat"));
        suite.addTest(new XMLLexerFormatterTest("testFormatSubsection"));
        suite.addTest(new XMLLexerFormatterTest("testFormatForTab"));
        suite.addTest(new XMLLexerFormatterTest("testFormatPerformance"));
        suite.addTest(new XMLLexerFormatterTest("testFormatSubsection1"));
        suite.addTest(new XMLLexerFormatterTest("testFormatSubsection2"));
        suite.addTest(new XMLLexerFormatterTest("testFormat_PreserveWhitespace"));
        suite.addTest(new XMLLexerFormatterTest("testFormat_WithNestedPreserveWhitespace"));
        suite.addTest(new XMLLexerFormatterTest("testFormatSubsection_PreserveWhitespace"));
        suite.addTest(new XMLLexerFormatterTest("testFormat_ContentIndent"));
        suite.addTest(new XMLLexerFormatterTest("testFormat_AttrsIndent"));
        suite.addTest(new XMLLexerFormatterTest("testFormat_ProcessingIndent"));
        suite.addTest(new XMLLexerFormatterTest("testFormat_nestedSameElements"));
        suite.addTest(new XMLLexerFormatterTest("testFormatWithCdataContent217342"));
        suite.addTest(new XMLLexerFormatterTest("testFormatBreaksMixedContent216986"));
        return suite;
    }

    /**
     * Formats an input document and then compares the formatted doc
     * with a document that represents expected outcome.
     */
    public void testFormat() throws Exception {
        LineDocument inputDoc = getDocument("indent/input.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    /**
     * Cheks formatting when empty comment (blank line) is present
     * See defect #269073
     */
    public void testFormatEmptyComment() throws Exception {
        LineDocument inputDoc = getDocument("indent/input_emptyComment.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_emptyComment.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    public void testFormatSubsection() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_sub.xml");
        //format a subsection of the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 72, 97);
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_sub.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    // #139160
    public void testFormatForTab() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input2.xsd");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output2.xsd");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    public void testFormatPerformance() throws Exception {
        BaseDocument inputDoc = getDocument("indent/1998stats.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        long t1 = System.currentTimeMillis();
        formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        long t2 = System.currentTimeMillis();
        System.out.println("Time taken to format NFL XML in ms:: " + (t2 - t1));

        //try OTA Schema
        inputDoc = getDocument("indent/1998stats.xml");
        //format the inputDoc
        formatter = new XMLLexerFormatter(null);
        t1 = System.currentTimeMillis();
        formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        t2 = System.currentTimeMillis();
        System.out.println("Time taken to format OTA Schema in ms:: " + (t2 - t1));
    }

    public void testFormatSubsection1() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_sub1.xml");
        //format a subsection of the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 46, 74);
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_sub1.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    public void testFormatSubsection2() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_sub2.xml");
        //format a subsection of the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 63, 80);
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_sub2.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    // #170343
    public void testFormat_PreserveWhitespace() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_preserve.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_preserve.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    // #170343
    public void testFormat_WithNestedPreserveWhitespace() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_withpreserve.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_withpreserve.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    // #170343
    public void testFormatSubsection_PreserveWhitespace() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_preserve.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        System.out.println("SECTION:" + inputDoc.getText(91, 87));
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 91, 91 + 87);
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_preserve.xml");
        assertTrue(compare(formattedDoc, outputDoc));
    }


    public void testFormat_ContentIndent() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_contentIndent.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_contentIndent.xml");
        assertTrue(compare(formattedDoc, outputDoc));
    }

    public void testFormat_AttrsIndent() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_attrsIndent.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_attrsIndent.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    public void testFormat_ProcessingIndent() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_processingXml.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_processingXml.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }
    
    
    public void testFormat_nestedSameElements() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_nestedSame.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_nestedSame.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }
    
    public void testFormatWithCdataContent217342() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_cdataContent.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_cdataContent.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    public void testFormatBreaksMixedContent216986() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_breaksMixedContent.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_breaksMixedContent.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }
    
    public void testFormatNewlinesInTags217995() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_newlineInTags.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_newlineInTags.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }
    
    public void testFormatTagAtLineStartAfterContent238985() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_tagAtStatOfLine.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_tagAtStatOfLine.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }
}
