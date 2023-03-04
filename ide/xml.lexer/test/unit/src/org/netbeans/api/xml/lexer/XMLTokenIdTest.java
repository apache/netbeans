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
package org.netbeans.api.xml.lexer;

import junit.framework.*;

/**
 * The XMLTokenIdTest tests the parsing algorithm of XMLLexer.
 * Various tests include, sanity, regression, performance etc.
 * @author Samaresh (samaresh.panda@sun.com)
 */
public class XMLTokenIdTest extends AbstractTestCase {
    
    public XMLTokenIdTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLTokenIdTest("testTokens"));
        //regression tests on XMLLexer
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new XMLTokenIdTest("testParse1"));
        suite.addTest(new XMLTokenIdTest("testParse2"));
        suite.addTest(new XMLTokenIdTest("testParse3"));
        //measure performace
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new XMLTokenIdTest("testParsePerformance"));
        return suite;
    }
    
    /**
     * This test parses a xml/schema that was earlier failing.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=124731
     * See http://hg.netbeans.org/main?cmd=changeset;node=34612be91839
     */
    public void testParse1() throws Exception {
        javax.swing.text.Document document = getDocument("resources/UBL-CommonAggregateComponents-1.0.xsd");
        parse(document);
    }
    
    /**
     * This test parses a xml/schema that was earlier failing.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=125005
     * See http://hg.netbeans.org/main?cmd=changeset;node=dcd138bddc6c
     */
    public void testParse2() throws Exception {
        javax.swing.text.Document document = getDocument("resources/wsdl.xml");
        parse(document);
    }
    
    /**
     * This test parses a xml/schema that was earlier failing.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=139184
     */
    public void testParse3() throws Exception {
        javax.swing.text.Document document = getDocument("resources/test1.xml");
        parse(document);
    }
    
    /**
     * This test measures the performance of XMLLexer on healthcare schema.
     */
    public void testParsePerformance() throws Exception {
        javax.swing.text.Document document = getDocument("resources/fields.xsd");
        long start = System.currentTimeMillis();
        parse(document);
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse healthcare schema: " + (end-start) + "ms.");
    }
    
    /**
     * This test validates all tokens obtained by parsing test.xml against
     * an array of expected tokens.
     */
    public void testTokens() throws Exception {
        XMLTokenId[] expectedIds = {XMLTokenId.PI_START, XMLTokenId.PI_TARGET, XMLTokenId.WS, XMLTokenId.PI_CONTENT,
            XMLTokenId.PI_END, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.ARGUMENT, XMLTokenId.OPERATOR, XMLTokenId.VALUE,
            XMLTokenId.WS, XMLTokenId.ARGUMENT, XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG,
            XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.BLOCK_COMMENT, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG,
            XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT};
        
        javax.swing.text.Document document = getDocument("resources/test.xml");
        assertTokenSequence(document, expectedIds);
    }    
}
