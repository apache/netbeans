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
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 893, 918);
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
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 868, 896);
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        LineDocument outputDoc = getDocument("indent/output_sub1.xml");
        assertTrue (compare(formattedDoc, outputDoc));
    }

    public void testFormatSubsection2() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_sub2.xml");
        //format a subsection of the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 885, 899);
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
        System.out.println("SECTION:" + inputDoc.getText(913, 87));
        LineDocument formattedDoc = formatter.doReformat(inputDoc, 913, 913 + 87);
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
