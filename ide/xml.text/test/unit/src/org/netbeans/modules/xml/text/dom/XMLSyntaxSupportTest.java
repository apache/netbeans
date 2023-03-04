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

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.AbstractTestCase;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;

/**
 *
 * @author Samaresh
 */
public class XMLSyntaxSupportTest extends AbstractTestCase {

    public XMLSyntaxSupportTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLSyntaxSupportTest("testParseForeward"));
        suite.addTest(new XMLSyntaxSupportTest("testParseBackward"));
        suite.addTest(new XMLSyntaxSupportTest("testTokens"));
        return suite;
    }

    /**
     * Parses a valid xml documents and reads one node at a time.
     */
    public void testParseForeward() throws Exception {
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(getDocument("syntax/test.xml"));
        SyntaxElement se = support.getElementChain(1);
        StringBuilder actualResult = new StringBuilder();
        while( se != null) {
            actualResult.append("Class: " + se.getClass().getSimpleName() + " Offset: " + se.getElementOffset() + " Length: "+ se.getElementLength() + "\n");
            se = se.getNext();
        }
        assertEquals(getExpectedResultAsString("dom/result1.txt"), actualResult.toString());
    }
    
    public void testParseBackward() throws Exception {
        BaseDocument doc = getDocument("syntax/test.xml");
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        SyntaxElement se = support.getElementChain(doc.getLength()-1);
        StringBuilder actualResult = new StringBuilder();
        while( se != null) {
            actualResult.append("Class: " + se.getClass().getSimpleName() + " Offset: " + se.getElementOffset() + " Length: "+ se.getElementLength() + "\n");
            se = se.getPrevious();
        }
        assertEquals(getExpectedResultAsString("dom/result2.txt"), actualResult.toString());
    }

    public void testTokens() throws Exception {
        BaseDocument doc = getDocument("syntax/test.xml");
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        Token token = support.getPreviousToken(852);
        System.out.println("Token: " + token.id() + " Text: " + token.text());
        token = support.getPreviousToken(852 + 1);
        System.out.println("Token: " + token.id() + " Text: " + token.text());
        token = support.getPreviousToken(852 + 2);
        System.out.println("Token: " + token.id() + " Text: " + token.text());
    }

}
