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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.db.sql.support;

import org.netbeans.modules.db.test.DDLTestBase;

/**
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 *
 * This class is a set of tests to make sure we're quoting identifiers
 * correctly
 */
public class QuoterTest extends DDLTestBase {
    
    private SQLIdentifiers.Quoter quoter;
    
    public QuoterTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        quoter = SQLIdentifiers.createQuoter(getConnection().getMetaData());        
    }
        
    public void testNoQuoting() {
        String identifier = "YOUDONTNEEDTOQUOTEME2334252__1451";
        String expResult = identifier;
        String result = quoter.quoteIfNeeded(identifier);
        assertEquals(expResult, result);
    } 
    
    public void testSpaces() throws Exception {
        String identifier = "YesYou Need to quote me";
        String expResult = quote(identifier);
        
        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);        
    }
    
    public void testCasing() throws Exception {
        String identifier;
        
        // First, find out what kind of casing is done with unquoted
        // identifiers for this connection
        int caseRule = getUnquotedCaseRule();
        
        switch (caseRule) {
        case LC_RULE:
            identifier = "ABCDEFG";
            break;
        case UC_RULE:
            identifier = "abcdefg";
            break;
        default:
            // Nothing to test here
            return;
        }
        
        String expResult = quote(identifier);
        
        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }
    
    public void testNonAscii() throws Exception {
        // borrowed translated message from Derby message file :)
        String identifier = "abcdABCD0934" +
                "\u4f8b\u5916\u306e\u305f\u3081\u3001\u59cb\u52d5" +
                "\u306b\u5931\u6557\u3057\u307e\u3057\u305f\u3002 \u8a73\u7d30" +
                "\u306b\u3064\u3044\u3066\u306f\u3001\u6b21\u306e\u4f8b\u5916" +
                "\u3092\u53c2\u7167\u3057\u3066\u304f\u3060\u3055\u3044\u3002" +
                "09298719871";
        
        String expResult = quote(identifier);
        
        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }
    
    public void testDontQuoteQuoted() throws Exception {
        String identifier = quote("I am already quoted");
        
        String expResult = identifier;

        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }
    
    public void testNullIdentifier() throws Exception {
        try {
            quoter.quoteIfNeeded(null);
            fail("Expected a NullPointerException");
        } catch ( NullPointerException npe ) {
            // expected
        }
        try {
            quoter.quoteAlways(null);
            fail("Expected a NullPointerException");
        } catch ( NullPointerException npe ) {
            // expected
        }
        try {
            quoter.unquote(null);
            fail("Expected a NullPointerException");
        } catch ( NullPointerException npe ) {
            // expected
        }
    }
    
    public void testFirstCharIsUnderbar() throws Exception {
        String identifier = "_NO_UNDERBAR_AS_FIRST_CHAR";
        
        String expResult = quote(identifier);

        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }

    public void testFirstCharIsNumber() throws Exception {
        String identifier = "1NO_NUMBER123_AS_FIRST_CHAR";
        
        String expResult = quote(identifier);

        String result = quoter.quoteIfNeeded(identifier);
        
        assertEquals(expResult, result);
    }

    public void testUnquote() {
        String quoteString = quoter.getQuoteString();
        assertEquals("", quoter.unquote(""));
        assertEquals("", quoter.unquote(quoteString + quoteString));
        assertEquals("id", quoter.unquote("id"));
        assertEquals("id", quoter.unquote(quoteString + "id"));
        assertEquals("id", quoter.unquote("id" + quoteString));
        assertEquals("id", quoter.unquote(quoteString + "id" + quoteString));
    }

    // Check SQL99-Quoting
    public void testQuoteIdentifierContainingQuotingChar() {
        String quoteString = quoter.getQuoteString();

        String unquoted = "test" + quoteString + "xx";
        String quoted = quoteString + "test" + quoteString + quoteString + "xx"
                + quoteString;

        assertEquals(quoted, quoter.quoteAlways(unquoted));
        assertEquals(unquoted, quoter.unquote(quoted));

        String unquoted2 = "test" + quoteString + "xx" + quoteString + quoteString;
        String quoted2 = quoteString + "test" + quoteString + quoteString + "xx"
                + quoteString + quoteString + quoteString + quoteString + quoteString;

        assertEquals(quoted2, quoter.quoteAlways(unquoted2));
        assertEquals(unquoted2, quoter.unquote(quoted2));
    }
}
