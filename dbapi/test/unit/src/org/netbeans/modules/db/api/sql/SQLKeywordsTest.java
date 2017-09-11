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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.db.api.sql;

import junit.framework.*;

/**
 * Tests the SQLKeyword class, that is, it ensures that the is.*Keyword()
 * methods return true for all keywords. Hopefull this will catch someone
 * making the keyword lists not correctly ordered when adding a
 * possibly forgotten keyword.
 *
 * @author Andrei Badea
 */
public class SQLKeywordsTest extends TestCase {

    public SQLKeywordsTest(String testName) {
        super(testName);
    }

    public void testIsSQL99ReservedKeyword() {
        for (int i = 0; i < SQLKeywords.SQL99_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a reserved keyword", SQLKeywords.isSQL99ReservedKeyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a reserved keyword", SQLKeywords.isSQL99ReservedKeyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99ReservedKeyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99ReservedKeyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }

    public void testIsSQL99NonReservedKeyword() {
        for (int i = 0; i < SQLKeywords.SQL99_NON_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_NON_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a non-reserved keyword", SQLKeywords.isSQL99NonReservedKeyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a non-reserved keyword", SQLKeywords.isSQL99NonReservedKeyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99NonReservedKeyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99NonReservedKeyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }

    public void testIsSQL99Keyword() {
        for (int i = 0; i < SQLKeywords.SQL99_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
        }
        for (int i = 0; i < SQLKeywords.SQL99_NON_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99Keyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99Keyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }
}
