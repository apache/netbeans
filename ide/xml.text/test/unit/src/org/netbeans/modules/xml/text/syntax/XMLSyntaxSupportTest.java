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

package org.netbeans.modules.xml.text.syntax;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.AbstractTestCase;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
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
        suite.addTest(new XMLSyntaxSupportTest("testParse1"));
        suite.addTest(new XMLSyntaxSupportTest("testParse2"));
        suite.addTest(new XMLSyntaxSupportTest("testTokens"));
        return suite;
    }

    public void testParse1() throws Exception {
        XMLSyntaxSupport support = getSyntaxSupport("syntax/test.xml");
        SyntaxElement se = support.getElementChain(1);
        while( se != null) {
            System.out.println("Class: " + se.getClass().getSimpleName() + " Offset: " + se.getElementOffset() + " Length: "+ se.getElementLength());
            se = se.getNext();
        }
    }

    public void testParse2() throws Exception {
        XMLSyntaxSupport support = getSyntaxSupport("syntax/invalid.xml");
        SyntaxElement se = support.getElementChain(1);
        while( se != null) {
            System.out.println("Class: " + se.getClass().getSimpleName() + " Offset: " + se.getElementOffset() + " Length: "+ se.getElementLength());
            se = se.getNext();
        }
    }

    public void testTokens() throws Exception {
        XMLSyntaxSupport support = getSyntaxSupport("syntax/test.xml");
        Token<XMLTokenId> token = support.getPreviousToken(30);
        System.out.println("Token: " + token.id().name() + " Text: " + token.text());
        token = support.getPreviousToken(31);
        System.out.println("Token: " + token.id().name() + " Text: " + token.text());
        token = support.getPreviousToken(32);
        System.out.println("Token: " + token.id().name() + " Text: " + token.text());
    }
}
