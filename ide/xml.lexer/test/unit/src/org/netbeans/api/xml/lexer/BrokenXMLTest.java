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
public class BrokenXMLTest extends AbstractTestCase {
    
    public BrokenXMLTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new BrokenXMLTest("testTokens"));
        return suite;
    }
    
    /**
     * This test validates all tokens obtained by parsing test.xml against
     * an array of expected tokens.
     */
    public void testTokens() throws Exception {
        XMLTokenId[] expectedIds = {XMLTokenId.PI_START, XMLTokenId.PI_TARGET, XMLTokenId.WS, XMLTokenId.PI_CONTENT,
            XMLTokenId.PI_END, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TEXT, 
            XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.ERROR, 
            XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT  };
        javax.swing.text.Document document = getDocument("resources/broken.xml");
        assertTokenSequence(document, expectedIds);
    }

}
